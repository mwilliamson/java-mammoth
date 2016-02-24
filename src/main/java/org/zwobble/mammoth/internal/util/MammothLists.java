package org.zwobble.mammoth.internal.util;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import java.util.List;

public class MammothLists {
    public static <T> List<T> list() {
        return ImmutableList.of();
    }

    public static <T> List<T> list(T value1) {
        return ImmutableList.of(value1);
    }

    public static <T> List<T> list(T value1, T value2) {
        return ImmutableList.of(value1, value2);
    }

    public static <T> List<T> list(T value1, T value2, T value3) {
        return ImmutableList.of(value1, value2, value3);
    }

    public static <T> List<T> cons(T head, Iterable<T> tail) {
        return concat(list(head), tail);
    }

    public static <T> List<T> concat(Iterable<T> first, Iterable<T> second) {
        return ImmutableList.copyOf(Iterables.concat(first, second));
    }

    public static <T, R> List<R> eagerMap(Iterable<T> iterable, Function<T, R> function) {
        return ImmutableList.copyOf(Iterables.transform(iterable, function));
    }

    public static <T, R> List<R> eagerFlatMap(Iterable<T> iterable, Function<T, Iterable<R>> function) {
        return ImmutableList.copyOf(Iterables.concat(Iterables.transform(iterable, function)));
    }

    public static <T, R extends Comparable<R>> List<T> orderedBy(Iterable<T> iterable, Function<T, R> getKey) {
        return orderBy(getKey).sortedCopy(iterable);
    }

    private static <T, R extends Comparable<R>> Ordering<T> orderBy(Function<T, R> getKey) {
        return Ordering.from((first, second) -> getKey.apply(first).compareTo(getKey.apply(second)));
    }
}
