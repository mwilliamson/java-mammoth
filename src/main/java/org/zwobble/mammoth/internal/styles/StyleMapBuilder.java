package org.zwobble.mammoth.internal.styles;

import org.zwobble.mammoth.internal.documents.Break;
import org.zwobble.mammoth.internal.documents.Paragraph;
import org.zwobble.mammoth.internal.documents.Run;
import org.zwobble.mammoth.internal.documents.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StyleMapBuilder {
    private Optional<HtmlPath> underline;
    private Optional<HtmlPath> strikethrough;
    private Optional<HtmlPath> allCaps;
    private Optional<HtmlPath> smallCaps;
    private Optional<HtmlPath> bold;
    private Optional<HtmlPath> italic;
    private Optional<HtmlPath> commentReference;
    private final List<StyleMapping<Paragraph>> paragraphStyles = new ArrayList<>();
    private final List<StyleMapping<Run>> runStyles = new ArrayList<>();
    private final List<StyleMapping<Table>> tableStyles = new ArrayList<>();
    private final List<StyleMapping<Break>> breakStyles = new ArrayList<>();
    private final List<StyleMapping<String>> highlightStyles = new ArrayList<>();

    public StyleMapBuilder() {
        this.bold = Optional.empty();
        this.underline = Optional.empty();
        this.strikethrough = Optional.empty();
        this.allCaps = Optional.empty();
        this.smallCaps = Optional.empty();
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

    public StyleMapBuilder allCaps(HtmlPath path) {
        this.allCaps = Optional.of(path);
        return this;
    }

    public StyleMapBuilder smallCaps(HtmlPath path) {
        this.smallCaps = Optional.of(path);
        return this;
    }

    public StyleMapBuilder mapHighlight(HighlightMatcher matcher, HtmlPath path) {
        this.highlightStyles.add(new StyleMapping<>(matcher, path));
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

    public StyleMapBuilder mapTable(TableMatcher matcher, HtmlPath path) {
        tableStyles.add(new StyleMapping<>(matcher, path));
        return this;
    }

    public StyleMapBuilder mapBreak(BreakMatcher matcher, HtmlPath path) {
        breakStyles.add(new StyleMapping<>(matcher, path));
        return this;
    }

    public StyleMap build() {
        return new StyleMap(
            bold,
            italic,
            underline,
            strikethrough,
            allCaps,
            smallCaps,
            commentReference,
            paragraphStyles,
            runStyles,
            tableStyles,
            breakStyles,
            highlightStyles
        );
    }
}
