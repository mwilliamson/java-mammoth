package org.zwobble.mammoth.internal.styles;

import org.zwobble.mammoth.internal.documents.Break;
import org.zwobble.mammoth.internal.documents.Paragraph;
import org.zwobble.mammoth.internal.documents.Run;
import org.zwobble.mammoth.internal.documents.Table;
import org.zwobble.mammoth.internal.util.Lists;
import org.zwobble.mammoth.internal.util.Optionals;

import java.util.List;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.Iterables.tryFind;

public class StyleMap {
    public static StyleMapBuilder builder() {
        return new StyleMapBuilder();
    }

    public static StyleMap merge(StyleMap high, StyleMap low) {
        // TODO: add appropriate tests
        return new StyleMap(
            Optionals.first(high.bold, low.bold),
            Optionals.first(high.italic, low.italic),
            Optionals.first(high.underline, low.underline),
            Optionals.first(high.strikethrough, low.strikethrough),
            Optionals.first(high.allCaps, low.allCaps),
            Optionals.first(high.smallCaps, low.smallCaps),
            Optionals.first(high.commentReference, low.commentReference),
            Lists.eagerConcat(high.paragraphStyles, low.paragraphStyles),
            Lists.eagerConcat(high.runStyles, low.runStyles),
            Lists.eagerConcat(high.tableStyles, low.tableStyles),
            Lists.eagerConcat(high.breakStyles, low.breakStyles),
            Lists.eagerConcat(high.highlightStyles, low.highlightStyles)
        );
    }

    public static final StyleMap EMPTY = new StyleMapBuilder().build();

    private final Optional<HtmlPath> bold;
    private final Optional<HtmlPath> italic;
    private final Optional<HtmlPath> underline;
    private final Optional<HtmlPath> strikethrough;
    private final Optional<HtmlPath> allCaps;
    private final Optional<HtmlPath> smallCaps;
    private final Optional<HtmlPath> commentReference;
    private final List<StyleMapping<Paragraph>> paragraphStyles;
    private final List<StyleMapping<Run>> runStyles;
    private final List<StyleMapping<Table>> tableStyles;
    private final List<StyleMapping<Break>> breakStyles;
    private final List<StyleMapping<String>> highlightStyles;

    public StyleMap(
        Optional<HtmlPath> bold,
        Optional<HtmlPath> italic,
        Optional<HtmlPath> underline,
        Optional<HtmlPath> strikethrough,
        Optional<HtmlPath> allCaps,
        Optional<HtmlPath> smallCaps,
        Optional<HtmlPath> commentReference,
        List<StyleMapping<Paragraph>> paragraphStyles,
        List<StyleMapping<Run>> runStyles,
        List<StyleMapping<Table>> tableStyles,
        List<StyleMapping<Break>> breakStyles,
        List<StyleMapping<String>> highlightStyles
    )
    {
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.strikethrough = strikethrough;
        this.allCaps = allCaps;
        this.smallCaps = smallCaps;
        this.commentReference = commentReference;
        this.paragraphStyles = paragraphStyles;
        this.runStyles = runStyles;
        this.tableStyles = tableStyles;
        this.breakStyles = breakStyles;
        this.highlightStyles = highlightStyles;
    }

    public StyleMap update(StyleMap styleMap) {
        // TODO: add appropriate tests
        return merge(styleMap, this);
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

    public Optional<HtmlPath> getAllCaps() {
        return allCaps;
    }

    public Optional<HtmlPath> getSmallCaps() {
        return smallCaps;
    }

    public Optional<HtmlPath> getCommentReference() {
        return commentReference;
    }

    public Optional<HtmlPath> getParagraphHtmlPath(Paragraph paragraph) {
        return tryFind(paragraphStyles, styleMapping -> styleMapping.matches(paragraph))
            .map(StyleMapping::getHtmlPath);
    }

    public Optional<HtmlPath> getRunHtmlPath(Run run) {
        return tryFind(runStyles, styleMapping -> styleMapping.matches(run))
            .map(StyleMapping::getHtmlPath);
    }

    public Optional<HtmlPath> getTableHtmlPath(Table table) {
        return tryFind(tableStyles, styleMapping -> styleMapping.matches(table))
            .map(StyleMapping::getHtmlPath);
    }

    public Optional<HtmlPath> getBreakHtmlPath(Break breakElement) {
        return tryFind(breakStyles, styleMapping -> styleMapping.matches(breakElement))
            .map(StyleMapping::getHtmlPath);
    }

    public Optional<HtmlPath> getHighlightHtmlPath(String color) {
        return tryFind(highlightStyles, styleMapping -> styleMapping.matches(color))
            .map(StyleMapping::getHtmlPath);
    }
}
