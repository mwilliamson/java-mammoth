package org.zwobble.mammoth.internal.styles;

import org.zwobble.mammoth.internal.documents.Break;
import org.zwobble.mammoth.internal.documents.Paragraph;
import org.zwobble.mammoth.internal.documents.Run;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StyleMapBuilder {
    private Optional<HtmlPath> underline;
    private Optional<HtmlPath> strikethrough;
    private Optional<HtmlPath> bold;
    private Optional<HtmlPath> italic;
    private Optional<HtmlPath> commentReference;
    private final List<StyleMapping<Paragraph>> paragraphStyles = new ArrayList<>();
    private final List<StyleMapping<Run>> runStyles = new ArrayList<>();
    private final List<StyleMapping<Break>> breakStyles = new ArrayList<>();

    public StyleMapBuilder() {
        this.bold = Optional.empty();
        this.underline = Optional.empty();
        this.strikethrough = Optional.empty();
        this.italic = Optional.empty();
        this.commentReference = Optional.empty();
    }

    public StyleMapBuilder bold(HtmlPath path) {
        this.bold = Optional.of(path);
        return this;
    }

    public StyleMapBuilder italic(HtmlPath path) {
        this.italic = Optional.of(path);
        return this;
    }

    public StyleMapBuilder underline(HtmlPath path) {
        this.underline = Optional.of(path);
        return this;
    }

    public StyleMapBuilder strikethrough(HtmlPath path) {
        this.strikethrough = Optional.of(path);
        return this;
    }

    public StyleMapBuilder commentReference(HtmlPath path) {
        this.commentReference = Optional.of(path);
        return this;
    }

    public StyleMapBuilder mapParagraph(ParagraphMatcher matcher, HtmlPath path) {
        paragraphStyles.add(new StyleMapping<>(matcher, path));
        return this;
    }

    public StyleMapBuilder mapRun(RunMatcher matcher, HtmlPath path) {
        runStyles.add(new StyleMapping<>(matcher, path));
        return this;
    }

    public StyleMap build() {
        return new StyleMap(bold, italic, underline, strikethrough, commentReference, paragraphStyles, runStyles, breakStyles);
    }
}
