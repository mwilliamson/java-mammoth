package org.zwobble.mammoth.internal.docx;

import com.google.common.collect.ImmutableList;
import org.zwobble.mammoth.internal.documents.DocumentElement;
import org.zwobble.mammoth.internal.results.InternalResult;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.zwobble.mammoth.internal.util.MammothIterables.lazyConcat;
import static org.zwobble.mammoth.internal.util.MammothIterables.lazyFlatMap;
import static org.zwobble.mammoth.internal.util.MammothLists.eagerFlatMap;
import static org.zwobble.mammoth.internal.util.MammothLists.list;
import static org.zwobble.mammoth.internal.util.MammothLists.toList;

public class ReadResult {
    public static final ReadResult EMPTY_SUCCESS = success(list());

    public static ReadResult concat(Iterable<ReadResult> results) {
        results = toList(results);
        return new ReadResult(
            eagerFlatMap(results, result -> result.elements),
            eagerFlatMap(results, result -> result.extra),
            lazyFlatMap(results, result -> result.warnings));
    }

    public static <T> ReadResult map(
        InternalResult<T> first,
        ReadResult second,
        BiFunction<T, List<DocumentElement>, DocumentElement> function)
    {
        return new ReadResult(
            list(function.apply(first.getValue(), second.elements)),
            second.extra,
            lazyConcat(first.getWarnings(), second.warnings));
    }

    public static ReadResult success(DocumentElement element) {
        return success(list(element));
    }

    public static ReadResult success(List<DocumentElement> elements) {
        return new ReadResult(elements, list(), list());
    }

    public static ReadResult emptyWithWarning(String warning) {
        return new ReadResult(list(), list(), list(warning));
    }

    public static ReadResult withWarning(DocumentElement element, String warning) {
        return new ReadResult(list(element), list(), list(warning));
    }

    private final List<DocumentElement> elements;
    private final List<DocumentElement> extra;
    private final Iterable<String> warnings;

    public ReadResult(List<DocumentElement> elements, List<DocumentElement> extra, Iterable<String> warnings) {
        this.elements = elements;
        this.extra = extra;
        this.warnings = warnings;
    }

    public ReadResult map(Function<List<DocumentElement>, DocumentElement> function) {
        return new ReadResult(
            list(function.apply(elements)),
            extra,
            warnings);
    }

    public ReadResult toExtra() {
        return new ReadResult(list(), concat(extra, elements), warnings);
    }

    public ReadResult appendExtra() {
        return new ReadResult(concat(elements, extra), list(), warnings);
    }

    public InternalResult<List<DocumentElement>> toResult() {
        return new InternalResult<>(elements, warnings);
    }

    private static <T> List<T> concat(List<T> first, List<T> second) {
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        builder.addAll(first);
        builder.addAll(second);
        return builder.build();
    }
}
