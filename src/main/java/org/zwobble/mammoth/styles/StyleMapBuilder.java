package org.zwobble.mammoth.styles;

import com.google.common.collect.ImmutableMap;

public class StyleMapBuilder {
    private HtmlPath underline;
    private HtmlPath strikethrough;
    private final ImmutableMap.Builder<String, HtmlPath> paragraphStyles = ImmutableMap.builder();

    public StyleMapBuilder() {
        this.underline = HtmlPath.EMPTY;
        this.strikethrough = HtmlPath.collapsibleElement("s");
    }

    public StyleMapBuilder underline(HtmlPath path) {
        this.underline = path;
        return this;
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
