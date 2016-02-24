package org.zwobble.mammoth.internal.util;

import com.google.common.collect.Iterables;

import java.util.Optional;

public class MammothIterables {
    public static <T> Optional<T> tryGetLast(Iterable<T> iterable) {
        return Optional.ofNullable(Iterables.getLast(iterable, null));
    }
}
