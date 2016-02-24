package org.zwobble.mammoth.internal.docx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.zwobble.mammoth.internal.documents.DocumentElement;
import org.zwobble.mammoth.internal.results.InternalResult;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.zwobble.mammoth.internal.util.MammothLists.list;
import static org.zwobble.mammoth.internal.util.MammothSets.set;

public class ReadResult {
    public static final ReadResult EMPTY_SUCCESS = success(list());

    public static ReadResult concat(Iterable<ReadResult> results) {
        ImmutableList.Builder<DocumentElement> elements = ImmutableList.builder();
        ImmutableList.Builder<DocumentElement> extra = ImmutableList.builder();
        ImmutableSet.Builder<String> warnings = ImmutableSet.builder();
        for (ReadResult result : results) {
            elements.addAll(result.elements);
            extra.addAll(result.extra);
            warnings.addAll(result.warnings);
        }
        return new ReadResult(elements.build(), extra.build(), warnings.build());
    }

    public static <T> ReadResult map(
        InternalResult<T> first,
        ReadResult second,
        BiFunction<T, List<DocumentElement>, DocumentElement> function)
    {
        return new ReadResult(
            list(function.apply(first.getValue(), second.elements)),
            second.extra,
            Sets.union(first.getWarnings(), second.warnings).immutableCopy());
    }

    public static ReadResult success(DocumentElement element) {
        return success(list(element));
    }

    public static ReadResult success(List<DocumentElement> elements) {
        return new ReadResult(elements, list(), set());
    }

    public static ReadResult emptyWithWarning(String warning) {
        return new ReadResult(list(), list(), set(warning));
    }

    public static ReadResult withWarning(DocumentElement element, String warning) {
        return new ReadResult(list(element), list(), set(warning));
    }

    private final List<DocumentElement> elements;
    private final List<DocumentElement> extra;
    private final Set<String> warnings;

    public ReadResult(List<DocumentElement> elements, List<DocumentElement> extra, Set<String> warnings) {
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
