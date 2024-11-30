package org.zwobble.mammoth.internal.styles;

import org.zwobble.mammoth.internal.documents.Alignment;
import org.zwobble.mammoth.internal.documents.NumberingLevel;
import org.zwobble.mammoth.internal.documents.Paragraph;

import java.util.Optional;

public class ParagraphMatcher implements DocumentElementMatcher<Paragraph> {
    public static final ParagraphMatcher ANY = new ParagraphMatcher(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public static ParagraphMatcher styleId(String styleId) {
        return new ParagraphMatcher(Optional.of(styleId), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static ParagraphMatcher styleName(String styleName) {
        return styleName(new EqualToStringMatcher(styleName));
    }

    public static ParagraphMatcher styleName(StringMatcher styleName) {
        return new ParagraphMatcher(Optional.empty(), Optional.of(styleName), Optional.empty(), Optional.empty());
    }

    public static ParagraphMatcher orderedList(String level) {
        return new ParagraphMatcher(Optional.empty(), Optional.empty(), Optional.of(NumberingLevel.ordered(level)), Optional.empty());
    }

    public static ParagraphMatcher unorderedList(String level) {
        return new ParagraphMatcher(Optional.empty(), Optional.empty(), Optional.of(NumberingLevel.unordered(level)), Optional.empty());
    }

    private final Optional<String> styleId;
    private final Optional<StringMatcher> styleName;
    private final Optional<NumberingLevel> numbering;
    private final Optional<StringMatcher> textAlign;

    public ParagraphMatcher(Optional<String> styleId, Optional<StringMatcher> styleName, Optional<NumberingLevel> numbering, Optional<StringMatcher> textAlign) {
        this.styleId = styleId;
        this.styleName = styleName;
        this.numbering = numbering;
        this.textAlign = textAlign;
    }

    @Override
    public boolean matches(Paragraph paragraph) {
        return matchesStyle(paragraph) && matchesNumbering(paragraph) && matchesTextAlign(paragraph);
    }

    private boolean matchesStyle(Paragraph paragraph) {
        return DocumentElementMatching.matchesStyle(styleId, styleName, paragraph.getStyle());
    }

    private boolean matchesNumbering(Paragraph paragraph) {
        return DocumentElementMatching.matches(numbering, paragraph.getNumbering(), (first, second) ->
            first.isOrdered() == second.isOrdered() && first.getLevelIndex().equalsIgnoreCase(second.getLevelIndex()));
    }

    private boolean matchesTextAlign(Paragraph paragraph) {
        Optional<String> alignment = paragraph.getAlignment().map(x -> x.getValue());
        return DocumentElementMatching.matches(textAlign, alignment, (first, second) -> first.matches(second));
    }
}
