package org.zwobble.mammoth.internal.util;

import java.util.Iterator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Iterables {
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

    public static <T, R> Iterable<R> lazyMapWithIndex(Iterable<T> iterable, BiFunction<Integer, T, R> function) {
        return new Iterable<R>() {
            @Override
            public Iterator<R> iterator() {
                return map(iterable.iterator(), function);
            }
        };
    }

    public static <T> Optional<T> getFirst(Iterable<? extends T> iterable) {
        Iterator<? extends T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        } else {
            return Optional.empty();
        }
    }

    public static <T> T getFirst(Iterable<? extends T> iterable, T defaultValue) {
        Iterator<? extends T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return defaultValue;
        }
    }

    public static <T> Optional<T> tryGetLast(Iterable<? extends T> iterable) {
        Iterator<? extends T> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        }

        while (true) {
            T last = iterator.next();
            if (!iterator.hasNext()) {
                return Optional.of(last);
            }
        }
    }

    public static <T, R> Iterable<R> lazyFlatMap(Iterable<T> iterable, Function<T, Iterable<R>> function) {
        return lazyFlatten(lazyMap(iterable, function));
    }

    public static <T> Iterable<T> lazyConcat(Iterable<T> iterable1, Iterable<T> iterable2) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return Stream.concat(stream(iterable1), stream(iterable2)).iterator();
            }
        };
    }

    public static <T> Iterable<T> lazyFlatten(Iterable<? extends Iterable<T>> iterables) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return stream(iterables)
                    .flatMap(iterable -> stream(iterable))
                    .iterator();
            }
        };
    }

    public static <T> Iterable<T> lazyFilter(Iterable<T> iterables, Predicate<T> predicate) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return stream(iterables)
                    .filter(predicate)
                    .iterator();
            }
        };
    }

    public static <T, R> Iterable<R> lazyFilter(Iterable<T> iterables, Class<R> clazz) {
        return new Iterable<R>() {
            @Override
            @SuppressWarnings("unchecked")
            public Iterator<R> iterator() {
                return (Iterator<R>) stream(iterables)
                    .filter(clazz::isInstance)
                    .iterator();
            }
        };
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

    private static <T, R> Iterator<R> map(Iterator<T> iterator, BiFunction<Integer, T, R> function) {
        return new Iterator<R>() {
            private int nextIndex = 0;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public R next() {
                return function.apply(nextIndex++, iterator.next());
            }
        };
    }

    public static <T> boolean any(Iterable<T> iterable, Predicate<T> predicate) {
        return stream(iterable).anyMatch(predicate);
    }

    public static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static <T> OptionalInt findIndex(Iterable<T> iterable, Predicate<T> predicate) {
        int index = 0;
        for (T element : iterable) {
            if (predicate.test(element)) {
                return OptionalInt.of(index);
            }
            index++;
        }
        return OptionalInt.empty();
    }
}
