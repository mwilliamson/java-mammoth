package org.zwobble.mammoth.tests;

import org.zwobble.mammoth.internal.util.Iterables;

import java.util.Iterator;

import static java.util.Arrays.asList;

public class Arguments {
    private final Object[] arguments;

    public Arguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public <T> T get(Class<T> clazz) {
        return only(getAll(clazz));
    }

    public <T> T get(Class<T> clazz, T defaultValue) {
        return onlyOrDefault(getAll(clazz), defaultValue);
    }

    public <T> T get(ArgumentKey<T> key, T defaultValue) {
        return onlyOrDefault(getAll(key), defaultValue);
    }

    private <T> Iterable<T> getAll(Class<T> clazz) {
        return Iterables.lazyFilter(asList(arguments), clazz);
    }

    private <T> Iterable<T> getAll(ArgumentKey<T> key) {
        return Iterables.lazyMap(
            Iterables.lazyFilter(
                Iterables.lazyFilter(asList(arguments), Argument.class),
                argument -> argument.getKey().equals(key)
            ),
            argument -> (T)argument.getValue()
        );
    }

    private <T> T onlyOrDefault(Iterable<T> iterable, T defaultValue) {
        return onlyOrDefault(iterable.iterator(), defaultValue);
    }

    private <T> T onlyOrDefault(Iterator<T> iterator, T defaultValue) {
        if (!iterator.hasNext()) {
            return defaultValue;
        } else {
            return only(iterator);
        }
    }

    private <T> T only(Iterable<T> iterable) {
        return only(iterable.iterator());
    }

    private <T> T only(Iterator<T> iterator) {
        T value = iterator.next();
        if (iterator.hasNext()) {
            throw new RuntimeException("Expected exactly one element, but had multiple elements");
        } else {
            return value;
        }
    }
}
