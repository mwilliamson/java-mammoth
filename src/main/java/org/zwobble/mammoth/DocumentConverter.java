package org.zwobble.mammoth;

import org.zwobble.mammoth.documents.*;
import org.zwobble.mammoth.html.Html;
import org.zwobble.mammoth.html.HtmlNode;

import java.util.List;

import static org.zwobble.mammoth.util.MammothLists.eagerFlatMap;
import static org.zwobble.mammoth.util.MammothLists.list;

public class DocumentConverter {
    public static List<HtmlNode> convertToHtml(Document document) {
        return convertChildrenToHtml(document);
    }

    private static List<HtmlNode> convertChildrenToHtml(HasChildren element) {
        return eagerFlatMap(
            element.getChildren(),
            DocumentConverter::convertToHtml);
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
