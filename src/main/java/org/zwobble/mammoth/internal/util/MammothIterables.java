package org.zwobble.mammoth.internal.util;

import com.google.common.collect.Iterables;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
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

    public static <T, R> Iterable<R> lazyMap(Iterable<T> iterable, Function<T, R> function) {
        return new Iterable<R>() {
            @Override
            public Iterator<R> iterator() {
                return map(iterable.iterator(), function);
            }
        };
    }

    public static <T> T getFirst(Iterable<? extends T> iterable, T defaultValue) {
        Iterator<? extends T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return defaultValue;
        }
    }

    private static <T, R> Iterator<R> map(Iterator<T> iterator, Function<T, R> function) {
        return new Iterator<R>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public R next() {
                return function.apply(iterator.next());
            }
        };
    }
}
