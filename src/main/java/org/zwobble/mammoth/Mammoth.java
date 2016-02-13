package org.zwobble.mammoth;

import com.google.common.base.Joiner;
import org.zwobble.mammoth.documents.*;
import org.zwobble.mammoth.docx.*;
import org.zwobble.mammoth.results.Result;
import org.zwobble.mammoth.xml.XmlElement;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import static com.google.common.collect.Iterables.transform;

public class Mammoth {
    public static Result<String> convertToHtml(File file) {
        try (DocxFile zipFile = new ZippedDocxFile(new ZipFile(file))) {
            XmlElement documentXml = OfficeXml.parseXml(zipFile.getInputStream("word/document.xml"));

            Styles styles = Styles.EMPTY;
            Numbering numbering = Numbering.EMPTY;
            Relationships relationships = Relationships.EMPTY;
            ContentTypes contentTypes = ContentTypes.DEFAULT;
            DocumentXmlReader reader = new DocumentXmlReader(new BodyXmlReader(
                styles,
                numbering,
                relationships,
                contentTypes,
                zipFile));
            return reader.readElement(documentXml)
                .map(Mammoth::convertToHtml);
        } catch (IOException e) {
            throw new UnsupportedOperationException("Should return a result of failure");   
        }
    }

    private static String convertToHtml(Document document) {
        return convertChildrenToHtml(document);
    }

    private static String convertChildrenToHtml(HasChildren element) {
        return Joiner.on("").join(transform(
            element.getChildren(),
            Mammoth::convertToHtml));
    }

    private static String convertToHtml(DocumentElement element) {
        return element.accept(new DocumentElementVisitor<String>() {
            @Override
            public String visit(Paragraph paragraph) {
                String content = convertChildrenToHtml(paragraph);
                if (content.isEmpty()) {
                    return "";
                } else {
                    return "<p>" + content + "</p>";
                }
            }

            @Override
            public String visit(Run run) {
                return convertChildrenToHtml(run);
            }

            @Override
            public String visit(Text text) {
                return text.getValue();
            }

            @Override
            public String visit(Tab tab) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(LineBreak lineBreak) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(Table table) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(TableRow tableRow) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(TableCell tableCell) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(Hyperlink hyperlink) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(Bookmark bookmark) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(NoteReference noteReference) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(Image image) {
                throw new UnsupportedOperationException();
            }
        });
    }
}
