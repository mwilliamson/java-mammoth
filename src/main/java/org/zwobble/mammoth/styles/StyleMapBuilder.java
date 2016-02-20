package org.zwobble.mammoth.styles;

import static org.zwobble.mammoth.util.MammothLists.list;

public class StyleMapBuilder {
    private HtmlPath underline;
    private HtmlPath strikethrough;

    public StyleMapBuilder() {
        this.underline = new HtmlPath(list());
        this.strikethrough = new HtmlPath(list(HtmlPath.collapsibleElement("s")));
    }

    public StyleMapBuilder underline(HtmlPathElement element) {
        return underline(new HtmlPath(list(element)));
    }

    public StyleMapBuilder underline(HtmlPath path) {
        this.underline = path;
        return this;
    }

    public StyleMapBuilder strikethrough(HtmlPathElement element) {
        return strikethrough(new HtmlPath(list(element)));
    }

    public StyleMapBuilder strikethrough(HtmlPath path) {
        this.strikethrough = path;
        return this;
    }

    public StyleMap build() {
        return new StyleMap(underline, strikethrough);
    }
}
