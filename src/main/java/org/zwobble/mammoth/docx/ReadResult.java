package org.zwobble.mammoth.docx;

import com.google.common.collect.ImmutableList;
import org.zwobble.mammoth.documents.DocumentElement;
import org.zwobble.mammoth.results.Result;
import org.zwobble.mammoth.results.Warning;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.zwobble.mammoth.util.MammothLists.list;

public class ReadResult {
    public static final ReadResult EMPTY_SUCCESS = success(list());

    public static ReadResult concat(Iterable<ReadResult> results) {
        ImmutableList.Builder<DocumentElement> elements = ImmutableList.builder();
        ImmutableList.Builder<DocumentElement> extra = ImmutableList.builder();
        ImmutableList.Builder<Warning> warnings = ImmutableList.builder();
        for (ReadResult result : results) {
            elements.addAll(result.elements);
            extra.addAll(result.extra);
            warnings.addAll(result.warnings);
        }
        return new ReadResult(elements.build(), extra.build(), warnings.build());
    }

    public static <T> ReadResult map(
        Result<T> first,
        ReadResult second,
        BiFunction<T, List<DocumentElement>, DocumentElement> function)
    {
        return new ReadResult(
            list(function.apply(first.getValue(), second.elements)),
            second.extra,
            concat(first.getWarnings(), second.warnings));
    }

    public static ReadResult success(DocumentElement element) {
        return success(list(element));
    }

    public static ReadResult success(List<DocumentElement> elements) {
        return new ReadResult(elements, list(), list());
    }

    public static ReadResult emptyWithWarning(Warning warning) {
        return new ReadResult(list(), list(), list(warning));
    }

    private final List<DocumentElement> elements;
    private final List<DocumentElement> extra;
    private final List<Warning> warnings;

    public ReadResult(List<DocumentElement> elements, List<DocumentElement> extra, List<Warning> warnings) {
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

    public Result<List<DocumentElement>> toResult() {
        return new Result<>(elements, warnings);
    }

    private static <T> List<T> concat(List<T> first, List<T> second) {
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        builder.addAll(first);
        builder.addAll(second);
        return builder.build();
    }
}
