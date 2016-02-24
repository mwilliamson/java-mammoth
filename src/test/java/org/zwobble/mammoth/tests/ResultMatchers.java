package org.zwobble.mammoth.tests;

import com.google.common.collect.ImmutableSet;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.zwobble.mammoth.Result;

import static org.hamcrest.Matchers.hasProperty;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;

public class ResultMatchers {
    public static Matcher<Result<?>> isResult(Matcher<?> valueMatcher, Iterable<String> warnings) {
        return Matchers.allOf(
            hasProperty("value", valueMatcher),
            hasWarnings(warnings));
    }

    public static Matcher<Result<?>> hasWarnings(Iterable<String> warnings) {
        return hasProperty("warnings", deepEquals(ImmutableSet.copyOf(warnings)));
    }
}
