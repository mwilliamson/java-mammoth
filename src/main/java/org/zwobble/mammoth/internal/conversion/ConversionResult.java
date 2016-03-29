package org.zwobble.mammoth.internal.conversion;

import org.zwobble.mammoth.internal.documents.DocumentElement;
import org.zwobble.mammoth.internal.documents.NoteReference;
import org.zwobble.mammoth.internal.html.HtmlNode;
import org.zwobble.mammoth.internal.results.InternalResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.zwobble.mammoth.internal.util.Lists.*;

class ConversionResult {
    static final ConversionResult EMPTY_SUCCESS = success(list());

    static <T> ConversionResult flatMap(List<T> elements, Function<T, ConversionResult> function) {
        return flatten(eagerMap(elements, function));
    }

    private static ConversionResult flatten(List<ConversionResult> results) {
        return new ConversionResult(eagerFlatMap(results, result -> result.nodes));
    }

    static <T> ConversionResult map(
        InternalResult<T> result1,
        ConversionResult result2,
        BiFunction<T, List<HtmlNode>, List<HtmlNode>> function)
    {
        return new ConversionResult(function.apply(result1.getValue(), result2.nodes));
    }

    static ConversionResult success(HtmlNode node) {
        return new ConversionResult(list(node));
    }

    static ConversionResult success(List<HtmlNode> nodes) {
        return new ConversionResult(nodes);
    }

    private final List<HtmlNode> nodes;
    private final List<NoteReference> noteReferences = new ArrayList<>();
    private final Set<String> warnings = new HashSet<>();

    ConversionResult(List<HtmlNode> nodes) {
        this.nodes = nodes;
    }

    ConversionResult map(Function<List<HtmlNode>, List<HtmlNode>> function) {

    }

    ConversionResult flatMap(Function<List<HtmlNode>, ConversionResult> function) {

    }

    InternalResult<List<HtmlNode>> toInternalResult() {
        return new InternalResult<>(nodes, list());
    }
}
