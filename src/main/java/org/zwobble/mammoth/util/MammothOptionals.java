package org.zwobble.mammoth.util;

import java.util.Optional;
import java.util.function.BiFunction;

public class MammothOptionals {
    public static <T1, T2, R> Optional<R> flatMap(
        Optional<T1> first,
        Optional<T2> second,
        BiFunction<T1, T2, Optional<R>> function)
    {
        if (first.isPresent() && second.isPresent()) {
            return function.apply(first.get(), second.get());
        } else {
            return Optional.empty();
        }
    }

    public static <T1, T2, R> Optional<R> map(
        Optional<T1> first,
        Optional<T2> second,
        BiFunction<T1, T2, R> function)
    {
        if (first.isPresent() && second.isPresent()) {
            return Optional.of(function.apply(first.get(), second.get()));
        } else {
            return Optional.empty();
        }
    }
}
