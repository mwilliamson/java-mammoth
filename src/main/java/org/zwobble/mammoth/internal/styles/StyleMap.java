package org.zwobble.mammoth.internal.styles;

import com.google.common.collect.Iterables;
import org.zwobble.mammoth.internal.documents.Paragraph;
import org.zwobble.mammoth.internal.documents.Run;

import java.util.List;
import java.util.Optional;

public class StyleMap {
    public static StyleMapBuilder builder() {
        return new StyleMapBuilder();
    }

    public static final StyleMap EMPTY = new StyleMapBuilder().build();

    private final HtmlPath underline;
    private final HtmlPath strikethrough;
    private final List<StyleMapping<Paragraph>> paragraphStyles;
    private final List<StyleMapping<Run>> runStyles;

    public StyleMap(
        HtmlPath underline,
        HtmlPath strikethrough,
        List<StyleMapping<Paragraph>> paragraphStyles,
        List<StyleMapping<Run>> runStyles)
    {
        this.underline = underline;
        this.strikethrough = strikethrough;
        this.paragraphStyles = paragraphStyles;
        this.runStyles = runStyles;
    }

    public HtmlPath getUnderline() {
        return underline;
    }

    public HtmlPath getStrikethrough() {
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
