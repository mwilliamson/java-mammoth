package org.zwobble.mammoth.internal.styles;

import org.zwobble.mammoth.internal.documents.Style;

import java.util.Optional;
import java.util.function.BiPredicate;

class DocumentElementMatching {
    static boolean matchesStyle(Optional<String> styleId, Optional<StringMatcher> styleName, Optional<Style> style) {
        return matchesStyleId(styleId, style) && matchesStyleName(styleName, style);
    }

    private static boolean matchesStyleId(Optional<String> styleId, Optional<Style> style) {
        return matches(styleId, style.map(Style::getStyleId), Object::equals);
    }

    private static boolean matchesStyleName(Optional<StringMatcher> styleName, Optional<Style> style) {
        return matches(styleName, style.flatMap(Style::getName), StringMatcher::matches);
    }

    static <T, U> boolean matches(Optional<T> required, Optional<U> actual, BiPredicate<T, U> areEqual) {
        return required
            .map(requiredValue -> actual.map(actualValue -> areEqual.test(requiredValue, actualValue)).orElse(false))
            .orElse(true);
    }
}
