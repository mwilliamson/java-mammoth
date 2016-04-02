package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.documents.Document;
import org.zwobble.mammoth.internal.documents.Notes;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.util.Lists;
import org.zwobble.mammoth.internal.xml.XmlElement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.Lists.list;

public class DocumentReader {
    @FunctionalInterface
    private interface BodyReaders {
        BodyXmlReader forName(String name);
    }

    public static InternalResult<Document> readDocument(Optional<Path> path, DocxFile zipFile) {
        Styles styles = readStyles(zipFile);
        Numbering numbering = readNumbering(zipFile);
        ContentTypes contentTypes = readContentTypes(zipFile);
        FileReader fileReader = new PathRelativeFileReader(path);
        BodyReaders bodyReaders = name -> {
            Relationships relationships = readRelationships(zipFile, name);
            return new BodyXmlReader(styles, numbering, relationships, contentTypes, zipFile, fileReader);
        };
        return readNotes(zipFile, bodyReaders)
            .flatMap(notes -> {
                DocumentXmlReader reader = new DocumentXmlReader(bodyReaders.forName("document"), notes);
                return reader.readElement(parseOfficeXml(zipFile, "word/document.xml"));
            });
    }

    private static InternalResult<Notes> readNotes(DocxFile file, BodyReaders bodyReaders) {
        return InternalResult.map(
            tryParseOfficeXml(file, "word/footnotes.xml")
                .map(NotesXmlReader.footnote(bodyReaders.forName("footnotes"))::readElement)
                .orElse(InternalResult.success(list())),
            tryParseOfficeXml(file, "word/endnotes.xml")
                .map(NotesXmlReader.endnote(bodyReaders.forName("endnotes"))::readElement)
                .orElse(InternalResult.success(list())),
            Lists::eagerConcat).map(Notes::new);
    }

    private static Styles readStyles(DocxFile file) {
        return tryParseOfficeXml(file, "word/styles.xml")
            .map(StylesXml::readStylesXmlElement)
            .orElse(Styles.EMPTY);
    }

    private static Numbering readNumbering(DocxFile file) {
        return tryParseOfficeXml(file, "word/numbering.xml")
            .map(NumberingXml::readNumberingXmlElement)
            .orElse(Numbering.EMPTY);
    }

    private static ContentTypes readContentTypes(DocxFile file) {
        return tryParseOfficeXml(file, "[Content_Types].xml")
            .map(ContentTypesXml::readContentTypesXmlElement)
            .orElse(ContentTypes.DEFAULT);
    }

    private static Relationships readRelationships(DocxFile zipFile, String name) {
        return tryParseOfficeXml(zipFile, "word/_rels/" + name + ".xml.rels")
            .map(RelationshipsXml::readRelationshipsXmlElement)
            .orElse(Relationships.EMPTY);
    }

    private static Optional<XmlElement> tryParseOfficeXml(DocxFile zipFile, String name) {
        try {
            return zipFile.tryGetInputStream(name).map(OfficeXml::parseXml);
        } catch (IOException exception) {
            // TODO: wrap in more specific exception and catch at the top
            throw new RuntimeException(exception);
        }
    }

    private static XmlElement parseOfficeXml(DocxFile zipFile, String name) {
        return tryParseOfficeXml(zipFile, name)
            // TODO: wrap in more specific exception and catch at the top
            .orElseThrow(() -> new RuntimeException(new IOException("Missing entry in file: " + name)));
    }
}
