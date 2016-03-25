package org.zwobble.mammoth.internal.styles;

import org.zwobble.mammoth.internal.documents.Paragraph;
import org.zwobble.mammoth.internal.documents.Run;
import org.zwobble.mammoth.internal.util.MammothLists;
import org.zwobble.mammoth.internal.util.MammothOptionals;

import java.util.List;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.MammothIterables.tryFind;

public class StyleMap {
    public static StyleMapBuilder builder() {
        return new StyleMapBuilder();
    }

    public static final StyleMap EMPTY = new StyleMapBuilder().build();

    private final Optional<HtmlPath> bold;
    private final Optional<HtmlPath> italic;
    private final Optional<HtmlPath> underline;
    private final Optional<HtmlPath> strikethrough;
    private final List<StyleMapping<Paragraph>> paragraphStyles;
    private final List<StyleMapping<Run>> runStyles;

    public StyleMap(
        Optional<HtmlPath> bold,
        Optional<HtmlPath> italic,
        Optional<HtmlPath> underline,
        Optional<HtmlPath> strikethrough,
        List<StyleMapping<Paragraph>> paragraphStyles,
        List<StyleMapping<Run>> runStyles)
    {
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.strikethrough = strikethrough;
        this.paragraphStyles = paragraphStyles;
        this.runStyles = runStyles;
    }

    public StyleMap update(StyleMap styleMap) {
        // TODO: add appropriate tests
        return new StyleMap(
            MammothOptionals.first(styleMap.bold, bold),
            MammothOptionals.first(styleMap.italic, italic),
            MammothOptionals.first(styleMap.underline, underline),
            MammothOptionals.first(styleMap.strikethrough, strikethrough),
            MammothLists.concat(styleMap.paragraphStyles, paragraphStyles),
            MammothLists.concat(styleMap.runStyles, runStyles));
    }

    public Optional<HtmlPath> getBold() {
        return bold;
    }

    public Optional<HtmlPath> getItalic() {
        return italic;
    }

    public Optional<HtmlPath> getUnderline() {
        return underline;
    }

    public Optional<HtmlPath> getStrikethrough() {
        return strikethrough;
    }

    public Optional<HtmlPath> getParagraphHtmlPath(Paragraph paragraph) {
        return tryFind(paragraphStyles, styleMapping -> styleMapping.matches(paragraph))
            .map(StyleMapping::getHtmlPath);
    }

    public Optional<HtmlPath> getRunHtmlPath(Run run) {
        return tryFind(runStyles, styleMapping -> styleMapping.matches(run))
            .map(StyleMapping::getHtmlPath);
    }
}
