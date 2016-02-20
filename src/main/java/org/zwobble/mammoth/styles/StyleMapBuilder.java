package org.zwobble.mammoth.styles;

import com.google.common.collect.ImmutableList;

public class StyleMapBuilder {
    private HtmlPath underline;
    private HtmlPath strikethrough;
    private final ImmutableList.Builder<StyleMapping> paragraphStyles = ImmutableList.builder();

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

    public StyleMapBuilder mapParagraph(ParagraphMatcher paragraphMatcher, HtmlPath path) {
        paragraphStyles.add(new StyleMapping(paragraphMatcher, path));
        return this;
    }

    public StyleMap build() {
        return new StyleMap(underline, strikethrough, paragraphStyles.build());
    }
}
