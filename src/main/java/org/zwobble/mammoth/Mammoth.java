package org.zwobble.mammoth;

import org.zwobble.mammoth.documents.*;
import org.zwobble.mammoth.docx.*;
import org.zwobble.mammoth.html.Html;
import org.zwobble.mammoth.html.HtmlNode;
import org.zwobble.mammoth.results.Result;
import org.zwobble.mammoth.xml.XmlElement;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipFile;

import static org.zwobble.mammoth.util.MammothLists.eagerFlatMap;
import static org.zwobble.mammoth.util.MammothLists.list;

public class Mammoth {
    public static Result<String> convertToHtml(File file) {
        try (DocxFile zipFile = new ZippedDocxFile(new ZipFile(file))) {
            XmlElement documentXml = OfficeXml.parseXml(zipFile.getInputStream("word/document.xml"));

            Styles styles = Styles.EMPTY;
            Numbering numbering = Numbering.EMPTY;
            Relationships relationships = Relationships.EMPTY;
            ContentTypes contentTypes = ContentTypes.DEFAULT;
            FileReader fileReader = new FileReader() {
                @Override
                public InputStream getInputStream(String uri) throws IOException {
                    throw new UnsupportedOperationException();
                }
            };
            DocumentXmlReader reader = new DocumentXmlReader(new BodyXmlReader(
                styles,
                numbering,
                relationships,
                contentTypes,
                zipFile,
                fileReader));
            return reader.readElement(documentXml)
                .map(Mammoth::convertToHtml)
                .map(Html::write);
        } catch (IOException e) {
            throw new UnsupportedOperationException("Should return a result of failure");   
        }
    }

    private static List<HtmlNode> convertToHtml(Document document) {
        return convertChildrenToHtml(document);
    }

    private static List<HtmlNode> convertChildrenToHtml(HasChildren element) {
        return eagerFlatMap(
            element.getChildren(),
            Mammoth::convertToHtml);
    }

    private static List<HtmlNode> convertToHtml(DocumentElement element) {
        return element.accept(new DocumentElementVisitor<List<HtmlNode>>() {
            @Override
            public List<HtmlNode> visit(Paragraph paragraph) {
                List<HtmlNode> content = convertChildrenToHtml(paragraph);
                if (content.isEmpty()) {
                    return list();
                } else {
                    return list(Html.element("p", content));
                }
            }

            @Override
            public List<HtmlNode> visit(Run run) {
                return convertChildrenToHtml(run);
            }

            @Override
            public List<HtmlNode> visit(Text text) {
                if (text.getValue().isEmpty()) {
                    return list();
                } else {
                    return list(Html.text(text.getValue()));
                }
            }

            @Override
            public List<HtmlNode> visit(Tab tab) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<HtmlNode> visit(LineBreak lineBreak) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<HtmlNode> visit(Table table) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<HtmlNode> visit(TableRow tableRow) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<HtmlNode> visit(TableCell tableCell) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<HtmlNode> visit(Hyperlink hyperlink) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<HtmlNode> visit(Bookmark bookmark) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<HtmlNode> visit(NoteReference noteReference) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<HtmlNode> visit(Image image) {
                throw new UnsupportedOperationException();
            }
        });
    }
}
