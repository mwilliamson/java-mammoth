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
        Iterator<T> arguments = getAll(clazz).iterator();
        if (!arguments.hasNext()) {
            return defaultValue;
        } else {
            return only(arguments);
        }
    }

    private <T> Iterable<T> getAll(Class<T> clazz) {
        return Iterables.lazyFilter(asList(arguments), clazz);
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
