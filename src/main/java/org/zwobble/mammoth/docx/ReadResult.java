package org.zwobble.mammoth.docx;

import com.google.common.collect.ImmutableList;
import org.zwobble.mammoth.Result;
import org.zwobble.mammoth.documents.DocumentElement;

import java.util.List;
import java.util.function.Function;

import static org.zwobble.mammoth.util.MammothLists.list;

public class ReadResult {
    public static final ReadResult EMPTY_SUCCESS = success(list());

    public static ReadResult concat(Iterable<ReadResult> results) {
        ImmutableList.Builder<DocumentElement> elements = ImmutableList.builder();
        for (ReadResult result : results) {
            elements.addAll(result.elements);
        }
        return new ReadResult(elements.build());
    }

    public static ReadResult success(DocumentElement element) {
        return success(list(element));
    }

    public static ReadResult success(List<DocumentElement> elements) {
        return new ReadResult(elements);
    }

    private final List<DocumentElement> elements;

    public ReadResult(List<DocumentElement> elements) {
        this.elements = elements;
    }

    public ReadResult map(Function<List<DocumentElement>, DocumentElement> function) {
        return new ReadResult(list(function.apply(elements)));
    }

    public Result<List<DocumentElement>> toResult() {
        return new Result<>(elements);
    }
}
