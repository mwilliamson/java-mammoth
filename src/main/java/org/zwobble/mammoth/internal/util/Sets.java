package org.zwobble.mammoth.internal.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.zwobble.mammoth.internal.util.Iterables.stream;

public class Sets {
    public static <T> Set<T> set() {
        return Collections.emptySet();
    }

    public static <T> Set<T> set(T value1) {
        return Collections.singleton(value1);
    }

    @SafeVarargs
    public static <T> Set<T> set(T... values) {
        HashSet<T> set = new HashSet<>();
        set.addAll(asList(values));
        return set;
    }

    public static <T> Set<T> difference(Set<? extends T> set1, Set<? extends T> set2) {
        HashSet<T> result = new HashSet<>(set1);
        result.removeAll(set2);
        return result;
    }

    public static <T> Set<T> toSet(Iterable<T> iterable) {
        return stream(iterable).collect(Collectors.toSet());
    }
}
