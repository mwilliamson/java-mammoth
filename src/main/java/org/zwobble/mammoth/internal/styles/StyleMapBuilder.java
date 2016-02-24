package org.zwobble.mammoth.internal.styles;

import com.google.common.collect.ImmutableList;
import org.zwobble.mammoth.internal.documents.Paragraph;
import org.zwobble.mammoth.internal.documents.Run;

import java.util.Optional;

public class StyleMapBuilder {
    private Optional<HtmlPath> underline;
    private Optional<HtmlPath> strikethrough;
    private final ImmutableList.Builder<StyleMapping<Paragraph>> paragraphStyles = ImmutableList.builder();
    private final ImmutableList.Builder<StyleMapping<Run>> runStyles = ImmutableList.builder();

    public StyleMapBuilder() {
        this.underline = Optional.empty();
        this.strikethrough = Optional.empty();
    }

    public StyleMapBuilder underline(HtmlPath path) {
        this.underline = Optional.of(path);
        return this;
    }

    public StyleMapBuilder strikethrough(HtmlPath path) {
        this.strikethrough = Optional.of(path);
        return this;
    }

    public StyleMapBuilder mapParagraph(ParagraphMatcher matcher, HtmlPath path) {
        paragraphStyles.add(new StyleMapping<Paragraph>(matcher, path));
        return this;
    }

    public StyleMapBuilder mapRun(RunMatcher matcher, HtmlPath path) {
        runStyles.add(new StyleMapping<Run>(matcher, path));
        return this;
    }

    public StyleMap build() {
        return new StyleMap(underline, strikethrough, paragraphStyles.build(), runStyles.build());
    }
}
