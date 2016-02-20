package org.zwobble.mammoth.styles;

import org.zwobble.mammoth.documents.NumberingLevel;
import org.zwobble.mammoth.documents.Paragraph;
import org.zwobble.mammoth.documents.Style;

import java.util.Optional;
import java.util.function.BiPredicate;

public class ParagraphMatcher {
    public static final ParagraphMatcher ANY = new ParagraphMatcher(Optional.empty(), Optional.empty(), Optional.empty());

    public static ParagraphMatcher styleId(String styleId) {
        return new ParagraphMatcher(Optional.of(styleId), Optional.empty(), Optional.empty());
    }

    public static ParagraphMatcher styleName(String styleName) {
        return new ParagraphMatcher(Optional.empty(), Optional.of(styleName), Optional.empty());
    }

    public static ParagraphMatcher orderedList(String level) {
        return new ParagraphMatcher(Optional.empty(), Optional.empty(), Optional.of(NumberingLevel.ordered(level)));
    }

    public static ParagraphMatcher unorderedList(String level) {
        return new ParagraphMatcher(Optional.empty(), Optional.empty(), Optional.of(NumberingLevel.unordered(level)));
    }

    private final Optional<String> styleId;
    private final Optional<String> styleName;
    private final Optional<NumberingLevel> numbering;

    public ParagraphMatcher(Optional<String> styleId, Optional<String> styleName, Optional<NumberingLevel> numbering) {
        this.styleId = styleId;
        this.styleName = styleName;
        this.numbering = numbering;
    }

    public boolean matches(Paragraph paragraph) {
        return matchesStyleId(paragraph) && matchesStyleName(paragraph) && matchesNumbering(paragraph);
    }

    private boolean matchesStyleId(Paragraph paragraph) {
        return matches(styleId, paragraph.getStyle().map(Style::getStyleId), Object::equals);
    }

    private boolean matchesStyleName(Paragraph paragraph) {
        return matches(styleName, paragraph.getStyle().flatMap(Style::getName), String::equalsIgnoreCase);
    }

    private boolean matchesNumbering(Paragraph paragraph) {
        return matches(numbering, paragraph.getNumbering(), (first, second) ->
            first.isOrdered() == second.isOrdered() && first.getLevelIndex().equalsIgnoreCase(second.getLevelIndex()));
    }

    private <T> boolean matches(Optional<T> required, Optional<T> actual, BiPredicate<T, T> areEqual) {
        return required
            .map(requiredValue -> actual.map(actualValue -> areEqual.test(requiredValue, actualValue)).orElse(false))
            .orElse(true);
    }
}
