package org.zwobble.mammoth.styles;

import com.google.common.collect.Iterables;
import org.zwobble.mammoth.documents.Paragraph;

import java.util.List;

public class StyleMap {
    public static StyleMapBuilder builder() {
        return new StyleMapBuilder();
    }

    public static final StyleMap EMPTY = new StyleMapBuilder().build();

    private final HtmlPath underline;
    private final HtmlPath strikethrough;
    private final List<StyleMapping> paragraphStyles;

    public StyleMap(HtmlPath underline, HtmlPath strikethrough, List<StyleMapping> paragraphStyles) {
        this.underline = underline;
        this.strikethrough = strikethrough;
        this.paragraphStyles = paragraphStyles;
    }

    public HtmlPath getUnderline() {
        return underline;
    }

    public HtmlPath getStrikethrough() {
        return strikethrough;
    }

    public HtmlPath getParagraphStyleMapping(Paragraph paragraph) {
        return Iterables.tryFind(paragraphStyles, styleMapping -> styleMapping.matches(paragraph))
            .transform(StyleMapping::getHtmlPath)
            .or(HtmlPath.element("p"));
    }
}
