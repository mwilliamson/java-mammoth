package org.zwobble.mammoth;

import org.zwobble.mammoth.documents.Notes;
import org.zwobble.mammoth.docx.*;
import org.zwobble.mammoth.html.Html;
import org.zwobble.mammoth.results.Result;
import org.zwobble.mammoth.xml.XmlElement;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.ZipFile;

public class Mammoth {
    public static Result<String> convertToHtml(File file) {
        try (DocxFile zipFile = new ZippedDocxFile(new ZipFile(file))) {
            Styles styles = readStyles(zipFile);
            Numbering numbering = Numbering.EMPTY;
            Relationships relationships = Relationships.EMPTY;
            ContentTypes contentTypes = ContentTypes.DEFAULT;
            Notes notes = Notes.EMPTY;
            FileReader fileReader = uri -> {
                throw new UnsupportedOperationException();
            };
            DocumentXmlReader reader = new DocumentXmlReader(new BodyXmlReader(
                styles,
                numbering,
                relationships,
                contentTypes,
                zipFile,
                fileReader), notes);
            // TODO: prefix
            String idPrefix = "document";
            return reader.readElement(parseOfficeXml(zipFile, "word/document.xml"))
                .map(nodes -> DocumentConverter.convertToHtml(idPrefix, nodes))
                .map(Html::stripEmpty)
                .map(Html::collapse)
                .map(Html::write);
        } catch (IOException e) {
            throw new UnsupportedOperationException("Should return a result of failure");   
        }
    }

    private static Styles readStyles(DocxFile file) throws IOException {
        return tryParseOfficeXml(file, "word/styles.xml")
            .map(StylesXml::readStylesXmlElement)
            .orElse(Styles.EMPTY);
    }

    private static Optional<XmlElement> tryParseOfficeXml(DocxFile zipFile, String name) throws IOException {
        return zipFile.tryGetInputStream(name).map(OfficeXml::parseXml);
    }

    private static XmlElement parseOfficeXml(DocxFile zipFile, String name) throws IOException {
        return tryParseOfficeXml(zipFile, name)
            .orElseThrow(() -> new IOException("Missing entry in file: " + name));
    }
}
