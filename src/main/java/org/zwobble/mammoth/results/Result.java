package org.zwobble.mammoth.results;

import com.google.common.collect.ImmutableList;
import org.zwobble.mammoth.documents.Style;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.zwobble.mammoth.util.MammothLists.list;

public class Result<T> {
    public static <T> Result<List<T>> concat(Iterable<Result<T>> results) {
        ImmutableList.Builder<T> elements = ImmutableList.builder();
        ImmutableList.Builder<Warning> warnings = ImmutableList.builder();
        for (Result<T> result : results) {
            elements.add(result.value);
            warnings.addAll(result.warnings);
        }
        return new Result<>(elements.build(), warnings.build());
    }

    public static Result<Optional<Style>> empty() {
        return new Result<>(Optional.empty(), list());
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, list());
    }

    private final T value;
    private final List<Warning> warnings;

    public Result(T value, List<Warning> warnings) {
        this.value = value;
        this.warnings = warnings;
    }

    public T getValue() {
        return value;
    }

    public List<Warning> getWarnings() {
        return warnings;
    }

    public <R> Result<R> map(Function<T, R> function) {
        return new Result<>(function.apply(value), warnings);
    }
}
