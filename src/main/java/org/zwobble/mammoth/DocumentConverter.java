package org.zwobble.mammoth;

import org.zwobble.mammoth.documents.*;
import org.zwobble.mammoth.html.Html;
import org.zwobble.mammoth.html.HtmlNode;

import java.util.List;
import java.util.Map;

import static org.zwobble.mammoth.util.MammothLists.eagerFlatMap;
import static org.zwobble.mammoth.util.MammothLists.list;
import static org.zwobble.mammoth.util.MammothMaps.map;

public class DocumentConverter {
    private final String idPrefix;

    public DocumentConverter(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    public List<HtmlNode> convertToHtml(Document document) {
        return convertChildrenToHtml(document);
    }

    private List<HtmlNode> convertChildrenToHtml(HasChildren element) {
        return eagerFlatMap(
            element.getChildren(),
            this::convertToHtml);
    }

    public List<HtmlNode> convertToHtml(DocumentElement element) {
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
                List<HtmlNode> nodes = convertChildrenToHtml(run);
                if (run.isStrikethrough()) {
                    nodes = list(Html.element("s", nodes));
                }
                if (run.getVerticalAlignment() == VerticalAlignment.SUBSCRIPT) {
                    nodes = list(Html.element("sub", nodes));
                }
                if (run.getVerticalAlignment() == VerticalAlignment.SUPERSCRIPT) {
                    nodes = list(Html.element("sup", nodes));
                }
                if (run.isItalic()) {
                    nodes = list(Html.element("em", nodes));
                }
                if (run.isBold()) {
                    nodes = list(Html.element("strong", nodes));
                }
                return nodes;
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
                return list(Html.text("\t"));
            }

            @Override
            public List<HtmlNode> visit(LineBreak lineBreak) {
                return list(Html.selfClosingElement("br"));
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
                Map<String, String> attributes = map("href", generateHref(hyperlink));
                return list(Html.element("a", attributes, convertChildrenToHtml(hyperlink)));
            }

            private String generateHref(Hyperlink hyperlink) {
                if (hyperlink.getHref().isPresent()) {
                    return hyperlink.getHref().get();
                } else if (hyperlink.getAnchor().isPresent()) {
                    return "#" + generateId(hyperlink.getAnchor().get());
                } else {
                    return "";
                }
            }

            @Override
            public List<HtmlNode> visit(Bookmark bookmark) {
                return list(Html.element("a", map("id", generateId(bookmark.getName()))));
            }

            private String generateId(String bookmarkName) {
                return idPrefix + "-" + bookmarkName;
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
