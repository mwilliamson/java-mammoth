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
        ImmutableList.Builder<Warning> warnings = ImmutableList.builder();
        for (ReadResult result : results) {
            elements.addAll(result.elements);
            warnings.addAll(result.warnings);
        }
        return new ReadResult(elements.build(), warnings.build());
    }

    public static <T> ReadResult map(
        Result<T> first,
        ReadResult second,
        BiFunction<T, List<DocumentElement>, DocumentElement> function)
    {
        ImmutableList.Builder<Warning> warnings = ImmutableList.builder();
        warnings.addAll(first.getWarnings());
        warnings.addAll(second.warnings);

        return new ReadResult(
            list(function.apply(first.getValue(), second.elements)),
            warnings.build());
    }

    public static ReadResult success(DocumentElement element) {
        return success(list(element));
    }

    public static ReadResult success(List<DocumentElement> elements) {
        return new ReadResult(elements, list());
    }

    private final List<DocumentElement> elements;
    private final List<Warning> warnings;

    public ReadResult(List<DocumentElement> elements, List<Warning> warnings) {
        this.elements = elements;
        this.warnings = warnings;
    }

    public ReadResult map(Function<List<DocumentElement>, DocumentElement> function) {
        return new ReadResult(list(function.apply(elements)), warnings);
    }

    public Result<List<DocumentElement>> toResult() {
        return new Result<>(elements, warnings);
    }
}
