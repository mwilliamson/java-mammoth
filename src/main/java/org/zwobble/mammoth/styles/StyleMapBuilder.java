package org.zwobble.mammoth.styles;

import com.google.common.collect.ImmutableMap;

import static org.zwobble.mammoth.util.MammothLists.list;

public class StyleMapBuilder {
    private HtmlPath underline;
    private HtmlPath strikethrough;
    private final ImmutableMap.Builder<String, HtmlPath> paragraphStyles = ImmutableMap.builder();

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

    public StyleMapBuilder mapParagraph(String styleId, HtmlPath path) {
        paragraphStyles.put(styleId, path);
        return this;
    }

    public StyleMap build() {
        return new StyleMap(underline, strikethrough, paragraphStyles.build());
    }
}
