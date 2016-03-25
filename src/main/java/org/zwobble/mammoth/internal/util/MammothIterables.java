package org.zwobble.mammoth.internal.util;

import com.google.common.collect.Iterables;

import java.util.Optional;
import java.util.function.Predicate;

public class MammothIterables {
    public static <T> Optional<T> tryGetLast(Iterable<T> iterable) {
        return Optional.ofNullable(Iterables.getLast(iterable, null));
    }

    public static <T> Optional<T> tryFind(Iterable<T> iterable, Predicate<T> predicate) {
        for (T element : iterable) {
            if (predicate.test(element)) {
                return Optional.of(element);
            }
        }
        return Optional.empty();
    }
}
