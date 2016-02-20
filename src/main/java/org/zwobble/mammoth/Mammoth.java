package org.zwobble.mammoth;

import org.zwobble.mammoth.documents.Notes;
import org.zwobble.mammoth.docx.*;
import org.zwobble.mammoth.html.Html;
import org.zwobble.mammoth.results.Result;
import org.zwobble.mammoth.util.MammothLists;
import org.zwobble.mammoth.xml.XmlElement;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.ZipFile;

import static org.zwobble.mammoth.util.MammothLists.list;

public class Mammoth {
    public static class Options {
        public static final Options DEFAULT = new Options("", false);

        private final String idPrefix;
        private final boolean preserveEmptyParagraphs;

        public Options(String idPrefix, boolean preserveEmptyParagraphs) {
            this.idPrefix = idPrefix;
            this.preserveEmptyParagraphs = preserveEmptyParagraphs;
        }

        public Options idPrefix(String prefix) {
            return new Options(prefix, preserveEmptyParagraphs);
        }

        public Options preserveEmptyParagraphs() {
            return new Options(idPrefix, true);
        }
    }

    @FunctionalInterface
    private interface BodyReaders {
        BodyXmlReader forName(String name);
    }

    public static Result<String> convertToHtml(File file) {
        return convertToHtml(file, Options.DEFAULT);
    }

    public static Result<String> convertToHtml(File file, Options options) {
        try (DocxFile zipFile = new ZippedDocxFile(new ZipFile(file))) {
            Styles styles = readStyles(zipFile);
            Numbering numbering = Numbering.EMPTY;
            ContentTypes contentTypes = ContentTypes.DEFAULT;
            FileReader fileReader = uri -> {
                throw new UnsupportedOperationException();
            };
            BodyReaders bodyReaders = name -> {
                Relationships relationships = readRelationships(zipFile, name);
                return new BodyXmlReader(styles, numbering, relationships, contentTypes, zipFile, fileReader);
            };
            return readNotes(zipFile, bodyReaders)
                .flatMap(notes -> {
                    DocumentXmlReader reader = new DocumentXmlReader(bodyReaders.forName("document"), notes);
                    return reader.readElement(parseOfficeXml(zipFile, "word/document.xml"));
                })
                .map(nodes -> DocumentConverter.convertToHtml(options.idPrefix, options.preserveEmptyParagraphs, nodes))
                .map(Html::stripEmpty)
                .map(Html::collapse)
                .map(Html::write);
        } catch (IOException e) {
            throw new UnsupportedOperationException("Should return a result of failure");   
        }
    }

    private static Result<Notes> readNotes(DocxFile file, BodyReaders bodyReaders) {
        return Result.map(
            tryParseOfficeXml(file, "word/footnotes.xml")
                .map(NotesXmlReader.footnote(bodyReaders.forName("footnotes"))::readElement)
                .orElse(Result.success(list())),
            tryParseOfficeXml(file, "word/endnotes.xml")
                .map(NotesXmlReader.endnote(bodyReaders.forName("endnotes"))::readElement)
                .orElse(Result.success(list())),
            MammothLists::concat).map(Notes::new);
    }

    private static Styles readStyles(DocxFile file) {
        return tryParseOfficeXml(file, "word/styles.xml")
            .map(StylesXml::readStylesXmlElement)
            .orElse(Styles.EMPTY);
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
