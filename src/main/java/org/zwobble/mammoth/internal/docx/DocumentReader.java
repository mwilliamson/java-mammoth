package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.archives.Archive;
import org.zwobble.mammoth.internal.archives.ZipPaths;
import org.zwobble.mammoth.internal.documents.Comment;
import org.zwobble.mammoth.internal.documents.Document;
import org.zwobble.mammoth.internal.documents.Notes;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.util.Lists;
import org.zwobble.mammoth.internal.util.PassThroughException;
import org.zwobble.mammoth.internal.xml.XmlElement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.zwobble.mammoth.internal.util.Lists.*;
import static org.zwobble.mammoth.internal.util.Strings.trimLeft;

public class DocumentReader {
    @FunctionalInterface
    private interface PartWithBodyReader {
        <T> T readPart(String name, BiFunction<XmlElement, BodyXmlReader, T> read, Optional<T> defaultValue);
    }

    public static InternalResult<Document> readDocument(Optional<Path> path, Archive zipFile) {
        PartPaths partPaths = findPartPaths(zipFile);

        Styles styles = readStyles(zipFile, partPaths);
        Numbering numbering = readNumbering(zipFile, partPaths);
        ContentTypes contentTypes = readContentTypes(zipFile);
        FileReader fileReader = new PathRelativeFileReader(path);
        PartWithBodyReader partReader = new PartWithBodyReader() {
            @Override
            public <T> T readPart(String name, BiFunction<XmlElement, BodyXmlReader, T> readPart, Optional<T> defaultValue) {
                Relationships relationships = readRelationships(zipFile, findRelationshipsPathFor(name));
                BodyXmlReader bodyReader = new BodyXmlReader(styles, numbering, relationships, contentTypes, zipFile, fileReader);
                if (defaultValue.isPresent()) {
                    return tryParseOfficeXml(zipFile, name)
                        .map(root -> readPart.apply(root, bodyReader))
                        .orElse(defaultValue.get());
                } else {
                    return readPart.apply(parseOfficeXml(zipFile, name), bodyReader);
                }
            }
        };
        return InternalResult.flatMap(
            readNotes(partReader, partPaths),
            readComments(partReader, partPaths),
            (notes, comments) -> partReader.readPart(
                partPaths.getMainDocument(),
                (element, bodyReader) -> new DocumentXmlReader(bodyReader, notes, comments).readElement(element),
                Optional.empty()
            )
        );
    }

    public static class PartPaths {
        private final String mainDocument;

        public PartPaths(String mainDocument) {
            this.mainDocument = mainDocument;
        }

        public String getMainDocument() {
            return mainDocument;
        }

        public String getComments() {
            return "word/comments.xml";
        }

        public String getEndnotes() {
            return "word/endnotes.xml";
        }

        public String getFootnotes() {
            return "word/footnotes.xml";
        }

        public String getNumbering() {
            return "word/numbering.xml";
        }

        public String getStyles() {
            return "word/styles.xml";
        }
    }

    private static PartPaths findPartPaths(Archive archive) {
        Relationships packageRelationships = readPackageRelationships(archive);
        String documentFilename = findDocumentFilename(archive, packageRelationships);
        return new PartPaths(documentFilename);
    }

    private static Relationships readPackageRelationships(Archive archive) {
        return readRelationships(archive, "_rels/.rels");
    }

    private static String findDocumentFilename(Archive archive, Relationships packageRelationships) {
        String officeDocumentType = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";
        List<String> targets = eagerConcat(
            eagerMap(
                packageRelationships.findTargetsByType(officeDocumentType),
                target -> trimLeft(target, '/')
            ),
            list("word/document.xml")
        );
        List<String> validTargets = eagerFilter(targets, archive::exists);
        return tryGetFirst(validTargets)
            .orElseThrow(() -> new PassThroughException(
                new IOException("Could not find main document part. Are you sure this is a valid .docx file?")
            ));
    }

    private static InternalResult<List<Comment>> readComments(PartWithBodyReader partReader, PartPaths partPaths) {
        return partReader.readPart(
            partPaths.getComments(),
            (root, bodyReader) -> new CommentXmlReader(bodyReader).readElement(root),
            Optional.of(InternalResult.success(list()))
        );
    }

    private static InternalResult<Notes> readNotes(PartWithBodyReader partReader, PartPaths partPaths) {
        return InternalResult.map(
            partReader.readPart(
                partPaths.getFootnotes(),
                (root, bodyReader) -> NotesXmlReader.footnote(bodyReader).readElement(root),
                Optional.of(InternalResult.success(list()))
            ),
            partReader.readPart(
                partPaths.getEndnotes(),
                (root, bodyReader) -> NotesXmlReader.endnote(bodyReader).readElement(root),
                Optional.of(InternalResult.success(list()))
            ),
            Lists::eagerConcat).map(Notes::new);
    }

    private static Styles readStyles(Archive file, PartPaths partPaths) {
        return tryParseOfficeXml(file, partPaths.getStyles())
            .map(StylesXml::readStylesXmlElement)
            .orElse(Styles.EMPTY);
    }

    private static Numbering readNumbering(Archive file, PartPaths partPaths) {
        return tryParseOfficeXml(file, partPaths.getNumbering())
            .map(NumberingXml::readNumberingXmlElement)
            .orElse(Numbering.EMPTY);
    }

    private static ContentTypes readContentTypes(Archive file) {
        return tryParseOfficeXml(file, "[Content_Types].xml")
            .map(ContentTypesXml::readContentTypesXmlElement)
            .orElse(ContentTypes.DEFAULT);
    }

    private static Relationships readRelationships(Archive zipFile, String name) {
        return tryParseOfficeXml(zipFile, name)
            .map(RelationshipsXml::readRelationshipsXmlElement)
            .orElse(Relationships.EMPTY);
    }

    private static String findRelationshipsPathFor(String name) {
        ZipPaths.SplitPath parts = ZipPaths.splitPath(name);
        return ZipPaths.joinPath(parts.getDirname(), "_rels", parts.getBasename() + ".rels");
    }

    private static Optional<XmlElement> tryParseOfficeXml(Archive zipFile, String name) {
        return PassThroughException.wrap(() ->
            zipFile.tryGetInputStream(name).map(OfficeXml::parseXml));
    }

    private static XmlElement parseOfficeXml(Archive zipFile, String name) {
        return tryParseOfficeXml(zipFile, name)
            .orElseThrow(() -> new PassThroughException(new IOException("Missing entry in file: " + name)));
    }
}
