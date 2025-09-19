package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.archives.Archive;
import org.zwobble.mammoth.internal.archives.ZipPaths;
import org.zwobble.mammoth.internal.documents.Comment;
import org.zwobble.mammoth.internal.documents.Document;
import org.zwobble.mammoth.internal.documents.Note;
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
import java.util.function.Function;

import static org.zwobble.mammoth.internal.util.Lists.*;
import static org.zwobble.mammoth.internal.util.Strings.trimLeft;

public class DocumentReader {
    public static InternalResult<Document> readDocument(
        Optional<Path> path,
        Archive zipFile,
        boolean externalFileAccess
    ) {
        PartPaths partPaths = findPartPaths(zipFile);

        Styles styles = readStyles(zipFile, partPaths);
        Numbering numbering = readNumbering(zipFile, partPaths, styles);
        ContentTypes contentTypes = readContentTypes(zipFile);

        FileReader fileReader = externalFileAccess
            ? PathRelativeFileReader.relativeTo(path)
            : DisabledFileReader.INSTANCE;

        PartWithBodyReader partReader = new PartWithBodyReader(zipFile, contentTypes, fileReader, numbering, styles);
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

    private static class PartWithBodyReader {
        private final Archive zipFile;
        private final ContentTypes contentTypes;
        private final FileReader fileReader;
        private final Numbering numbering;
        private final Styles styles;

        public PartWithBodyReader(
            Archive zipFile,
            ContentTypes contentTypes,
            FileReader fileReader,
            Numbering numbering,
            Styles styles
        ) {
            this.zipFile = zipFile;
            this.contentTypes = contentTypes;
            this.fileReader = fileReader;
            this.numbering = numbering;
            this.styles = styles;
        }

        <T> T readPart(String name, BiFunction<XmlElement, BodyXmlReader, T> readPart, Optional<T> defaultValue) {
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
    }

    public static class PartPaths {
        private final String mainDocument;
        private final String comments;
        private final String endnotes;
        private final String footnotes;
        private final String numbering;
        private final String styles;

        public PartPaths(String mainDocument, String comments, String endnotes, String footnotes, String numbering, String styles) {
            this.mainDocument = mainDocument;
            this.comments = comments;
            this.endnotes = endnotes;
            this.footnotes = footnotes;
            this.numbering = numbering;
            this.styles = styles;
        }

        public String getMainDocument() {
            return mainDocument;
        }

        public String getComments() {
            return comments;
        }

        public String getEndnotes() {
            return endnotes;
        }

        public String getFootnotes() {
            return footnotes;
        }

        public String getNumbering() {
            return numbering;
        }

        public String getStyles() {
            return styles;
        }
    }

    public static PartPaths findPartPaths(Archive archive) {
        Relationships packageRelationships = readPackageRelationships(archive);
        String documentFilename = findDocumentFilename(archive, packageRelationships);

        Relationships documentRelationships = readRelationships(
            archive,
            findRelationshipsPathFor(documentFilename)
        );

        Function<String, String> find = name -> findPartPath(
            archive,
            documentRelationships,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/" + name,
            ZipPaths.splitPath(documentFilename).getDirname(),
            "word/" + name + ".xml"
        );

        return new PartPaths(
            documentFilename,
            find.apply("comments"),
            find.apply("endnotes"),
            find.apply("footnotes"),
            find.apply("numbering"),
            find.apply("styles")
        );
    }

    private static Relationships readPackageRelationships(Archive archive) {
        return readRelationships(archive, "_rels/.rels");
    }

    private static String findDocumentFilename(Archive archive, Relationships packageRelationships) {
        String officeDocumentType = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";
        String mainDocumentPath = findPartPath(
            archive,
            packageRelationships,
            officeDocumentType,
            "",
            "word/document.xml"
        );

        if (archive.exists(mainDocumentPath)) {
            return mainDocumentPath;
        } else {
            throw new PassThroughException(
                new IOException("Could not find main document part. Are you sure this is a valid .docx file?")
            );
        }
    }

    private static String findPartPath(
        Archive archive,
        Relationships relationships,
        String relationshipType,
        String basePath,
        String fallbackPath
    ) {

        List<String> targets = eagerMap(
            relationships.findTargetsByType(relationshipType),
            target -> trimLeft(ZipPaths.joinPath(basePath, target), '/')
        );
        List<String> validTargets = eagerFilter(targets, archive::exists);
        return tryGetFirst(validTargets).orElse(fallbackPath);
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
                Optional.of(InternalResult.success(Lists.<Note>list()))
            ),
            partReader.readPart(
                partPaths.getEndnotes(),
                (root, bodyReader) -> NotesXmlReader.endnote(bodyReader).readElement(root),
                Optional.of(InternalResult.success(Lists.<Note>list()))
            ),
            Lists::eagerConcat).map(Notes::new);
    }

    private static Styles readStyles(Archive file, PartPaths partPaths) {
        return tryParseOfficeXml(file, partPaths.getStyles())
            .map(StylesXml::readStylesXmlElement)
            .orElse(Styles.EMPTY);
    }

    private static Numbering readNumbering(Archive file, PartPaths partPaths, Styles styles) {
        return tryParseOfficeXml(file, partPaths.getNumbering())
            .map((XmlElement element) -> NumberingXml.readNumberingXmlElement(element, styles))
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
