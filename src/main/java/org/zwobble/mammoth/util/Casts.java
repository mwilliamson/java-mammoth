package org.zwobble.mammoth.util;

import java.util.Optional;

public class Casts {
    public static <T> Optional<T> tryCast(Class<T> type, Object value) {
        try {
            return Optional.of(type.cast(value));
        } catch (ClassCastException exception) {
            return Optional.empty();
        }
    }
}
