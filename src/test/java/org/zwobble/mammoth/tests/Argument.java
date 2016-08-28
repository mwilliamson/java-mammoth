package org.zwobble.mammoth.tests;

public class Argument<T> {
    public static <T> Argument<T> arg(ArgumentKey<T> key, T value) {
        return new Argument<>(key, value);
    }

    private final ArgumentKey<T> key;
    private final T value;

    private Argument(ArgumentKey<T> key, T value) {
        this.key = key;
        this.value = value;
    }

    public ArgumentKey<T> getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }
}
