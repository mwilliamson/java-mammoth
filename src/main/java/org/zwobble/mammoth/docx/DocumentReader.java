package org.zwobble.mammoth.docx;

import org.zwobble.mammoth.documents.Document;
import org.zwobble.mammoth.documents.Notes;
import org.zwobble.mammoth.results.Result;
import org.zwobble.mammoth.util.MammothLists;
import org.zwobble.mammoth.xml.XmlElement;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;

import static org.zwobble.mammoth.util.MammothLists.list;

public class DocumentReader {
    @FunctionalInterface
    private interface BodyReaders {
        BodyXmlReader forName(String name);
    }

    public static Result<Document> readDocument(Optional<Path> path, DocxFile zipFile) {
        Styles styles = readStyles(zipFile);
        Numbering numbering = readNumbering(zipFile);
        ContentTypes contentTypes = readContentTypes(zipFile);
        FileReader fileReader = uri -> {
            try {
                Optional<URI> absoluteUri = asAbsoluteUri(uri);
                if (absoluteUri.isPresent()) {
                    return open(absoluteUri.get());
                } else if (path.isPresent()) {
                    return open(path.get().toUri().resolve(uri));
                } else {
                    throw new IOException("path of document is unknown, but is required for relative URI");
                }
            } catch (IOException exception) {
                throw new IOException("could not open external image '" + uri + "': " + exception.getMessage());
            }
        };
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

    private static InputStream open(URI uri) throws IOException {
        return uri.toURL().openStream();
    }

    private static Optional<URI> asAbsoluteUri(String uriString) {
        try {
            URI uri = new URI(uriString);
            return uri.isAbsolute() ? Optional.of(uri) : Optional.empty();
        } catch (URISyntaxException exception) {
            return Optional.empty();
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
