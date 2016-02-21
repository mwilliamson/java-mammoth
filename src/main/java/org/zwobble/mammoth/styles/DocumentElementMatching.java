package org.zwobble.mammoth.styles;

import org.zwobble.mammoth.documents.Style;

import java.util.Optional;
import java.util.function.BiPredicate;

class DocumentElementMatching {
    static boolean matchesStyle(Optional<String> styleId, Optional<String> styleName, Optional<Style> style) {
        return matchesStyleId(styleId, style) && matchesStyleName(styleName, style);
    }

    private static boolean matchesStyleId(Optional<String> styleId, Optional<Style> style) {
        return matches(styleId, style.map(Style::getStyleId), Object::equals);
    }

    private static boolean matchesStyleName(Optional<String> styleName, Optional<Style> style) {
        return matches(styleName, style.flatMap(Style::getName), String::equalsIgnoreCase);
    }

    static <T> boolean matches(Optional<T> required, Optional<T> actual, BiPredicate<T, T> areEqual) {
        return required
            .map(requiredValue -> actual.map(actualValue -> areEqual.test(requiredValue, actualValue)).orElse(false))
            .orElse(true);
    }
}
