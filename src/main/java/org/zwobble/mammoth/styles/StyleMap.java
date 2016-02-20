package org.zwobble.mammoth.styles;

import org.zwobble.mammoth.documents.Paragraph;

import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.util.MammothLists.list;

public class StyleMap {
    public static StyleMapBuilder builder() {
        return new StyleMapBuilder();
    }

    public static final StyleMap EMPTY = new StyleMapBuilder().build();

    private final HtmlPath underline;
    private final HtmlPath strikethrough;
    private final Map<String, HtmlPath> paragraphStyles;

    public StyleMap(HtmlPath underline, HtmlPath strikethrough, Map<String, HtmlPath> paragraphStyles) {
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
        return paragraph.getStyle()
            .flatMap(style -> Optional.ofNullable(paragraphStyles.get(style.getStyleId())))
            .orElse(new HtmlPath(list(HtmlPath.element("p"))));
    }
}
