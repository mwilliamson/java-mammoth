package org.zwobble.mammoth;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.function.Function;

public class Result<T> {
    public static <T> Result<List<T>> concat(Iterable<Result<T>> results) {
        ImmutableList.Builder<T> elements = ImmutableList.builder();
        for (Result<T> result : results) {
            elements.add(result.value);
        }
        return new Result<>(elements.build());
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value);
    }

    private final T value;

    public Result(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public <R> Result<R> map(Function<T, R> function) {
        return new Result<>(function.apply(value));
    }
}
