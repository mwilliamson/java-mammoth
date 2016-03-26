package org.zwobble.mammoth.internal.results;

import com.google.common.collect.ImmutableSet;
import org.zwobble.mammoth.Result;
import org.zwobble.mammoth.internal.documents.Style;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.zwobble.mammoth.internal.util.MammothIterables.lazyConcat;
import static org.zwobble.mammoth.internal.util.MammothIterables.lazyFlatMap;
import static org.zwobble.mammoth.internal.util.MammothLists.eagerMap;
import static org.zwobble.mammoth.internal.util.MammothLists.list;
import static org.zwobble.mammoth.internal.util.MammothLists.toList;

public class InternalResult<T> {
    public static <T> InternalResult<List<T>> flatten(Iterable<InternalResult<T>> results) {
        results = toList(results);
        return new InternalResult<>(
            eagerMap(results, result -> result.value),
            lazyFlatMap(results, result -> result.warnings));
    }

    public static <T1, T2, R> InternalResult<R> map(
        InternalResult<T1> first,
        InternalResult<T2> second,
        BiFunction<T1, T2, R> function)
    {
        return new InternalResult<>(
            function.apply(first.value, second.value),
            lazyConcat(first.warnings, second.warnings));
    }

    public static InternalResult<Optional<Style>> empty() {
        return new InternalResult<>(Optional.empty(), list());
    }

    public static <T> InternalResult<T> success(T value) {
        return new InternalResult<>(value, list());
    }

    private final T value;
    private final Iterable<String> warnings;

    public InternalResult(T value, Iterable<String> warnings) {
        this.value = value;
        this.warnings = warnings;
    }

    public T getValue() {
        return value;
    }

    public Iterable<String> getWarnings() {
        return warnings;
    }

    public <R> InternalResult<R> map(Function<T, R> function) {
        return new InternalResult<>(function.apply(value), warnings);
    }

    public <R> InternalResult<R> flatMap(Function<T, InternalResult<R>> function) {
        InternalResult<R> intermediateResult = function.apply(value);
        return new InternalResult<>(
            intermediateResult.value,
            lazyConcat(warnings, intermediateResult.warnings));
    }

    public Result<T> toResult() {
        Set<String> warnings = ImmutableSet.copyOf(this.warnings);
        return new Result<T>() {
            @Override
            public T getValue() {
                return value;
            }

            @Override
            public Set<String> getWarnings() {
                return warnings;
            }
        };
    }
}
