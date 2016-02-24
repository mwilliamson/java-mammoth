package org.zwobble.mammoth.internal.styles;

import com.google.common.collect.Iterables;
import org.zwobble.mammoth.internal.documents.Paragraph;
import org.zwobble.mammoth.internal.documents.Run;
import org.zwobble.mammoth.internal.util.MammothLists;
import org.zwobble.mammoth.internal.util.MammothOptionals;

import java.util.List;
import java.util.Optional;

public class StyleMap {
    public static StyleMapBuilder builder() {
        return new StyleMapBuilder();
    }

    public static final StyleMap EMPTY = new StyleMapBuilder().build();

    private final Optional<HtmlPath> underline;
    private final Optional<HtmlPath> strikethrough;
    private final List<StyleMapping<Paragraph>> paragraphStyles;
    private final List<StyleMapping<Run>> runStyles;

    public StyleMap(
        Optional<HtmlPath> underline,
        Optional<HtmlPath> strikethrough,
        List<StyleMapping<Paragraph>> paragraphStyles,
        List<StyleMapping<Run>> runStyles)
    {
        this.underline = underline;
        this.strikethrough = strikethrough;
        this.paragraphStyles = paragraphStyles;
        this.runStyles = runStyles;
    }

    public StyleMap update(StyleMap styleMap) {
        // TODO: add appropriate tests
        return new StyleMap(
            MammothOptionals.first(styleMap.underline, underline),
            MammothOptionals.first(styleMap.strikethrough, strikethrough),
            MammothLists.concat(styleMap.paragraphStyles, paragraphStyles),
            MammothLists.concat(styleMap.runStyles, runStyles));
    }

    public Optional<HtmlPath> getUnderline() {
        return underline;
    }

    public Optional<HtmlPath> getStrikethrough() {
        return strikethrough;
    }

    public Optional<HtmlPath> getParagraphHtmlPath(Paragraph paragraph) {
        com.google.common.base.Optional<StyleMapping<Paragraph>> mapping = Iterables.tryFind(
            paragraphStyles,
            styleMapping -> styleMapping.matches(paragraph));
        return Optional.ofNullable(mapping.orNull())
            .map(StyleMapping::getHtmlPath);
    }

    public Optional<HtmlPath> getRunHtmlPath(Run run) {
        com.google.common.base.Optional<StyleMapping<Run>> mapping = Iterables.tryFind(
            runStyles,
            styleMapping -> styleMapping.matches(run));
        return Optional.ofNullable(mapping.orNull())
            .map(StyleMapping::getHtmlPath);
    }
}
