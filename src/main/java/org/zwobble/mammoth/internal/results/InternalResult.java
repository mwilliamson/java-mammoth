package org.zwobble.mammoth.internal.results;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.zwobble.mammoth.internal.documents.Style;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.zwobble.mammoth.internal.util.MammothSets.set;

public class InternalResult<T> implements org.zwobble.mammoth.Result<T> {
    public static <T> InternalResult<List<T>> concat(Iterable<InternalResult<T>> results) {
        ImmutableList.Builder<T> elements = ImmutableList.builder();
        ImmutableSet.Builder<String> warnings = ImmutableSet.builder();
        for (InternalResult<T> result : results) {
            elements.add(result.value);
            warnings.addAll(result.warnings);
        }
        return new InternalResult<>(elements.build(), warnings.build());
    }

    public static <T1, T2, R> InternalResult<R> map(
        InternalResult<T1> first,
        InternalResult<T2> second,
        BiFunction<T1, T2, R> function)
    {
        return new InternalResult<>(
            function.apply(first.value, second.value),
            Sets.union(first.warnings, second.warnings).immutableCopy());
    }

    public static InternalResult<Optional<Style>> empty() {
        return new InternalResult<>(Optional.empty(), set());
    }

    public static <T> InternalResult<T> success(T value) {
        return new InternalResult<>(value, set());
    }

    private final T value;
    private final Set<String> warnings;

    public InternalResult(T value, Set<String> warnings) {
        this.value = value;
        this.warnings = warnings;
    }

    public T getValue() {
        return value;
    }

    public Set<String> getWarnings() {
        return warnings;
    }

    public <R> InternalResult<R> map(Function<T, R> function) {
        return new InternalResult<>(function.apply(value), warnings);
    }

    public <R> InternalResult<R> flatMap(Function<T, InternalResult<R>> function) {
        InternalResult<R> intermediateResult = function.apply(value);
        return new InternalResult<>(
            intermediateResult.value,
            Sets.union(warnings, intermediateResult.warnings).immutableCopy());
    }
}
