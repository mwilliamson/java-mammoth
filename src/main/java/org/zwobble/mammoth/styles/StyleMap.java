package org.zwobble.mammoth.styles;

public class StyleMap {
    public static StyleMapBuilder builder() {
        return new StyleMapBuilder();
    }

    public static final StyleMap EMPTY = new StyleMapBuilder().build();

    private final HtmlPath underline;
    private final HtmlPath strikethrough;

    public StyleMap(HtmlPath underline, HtmlPath strikethrough) {
        this.underline = underline;
        this.strikethrough = strikethrough;
    }

    public HtmlPath getUnderline() {
        return underline;
    }

    public HtmlPath getStrikethrough() {
        return strikethrough;
    }
}
