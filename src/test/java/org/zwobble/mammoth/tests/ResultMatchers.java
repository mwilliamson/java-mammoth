package org.zwobble.mammoth.tests;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.zwobble.mammoth.Result;
import org.zwobble.mammoth.internal.results.InternalResult;

import static org.hamcrest.Matchers.*;
import static org.zwobble.mammoth.internal.util.MammothLists.eagerMap;
import static org.zwobble.mammoth.internal.util.MammothLists.list;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;

public class ResultMatchers {
    public static Matcher<InternalResult<?>> isInternalSuccess(Object value) {
        return isInternalResult(deepEquals(value), list());
    }

    public static Matcher<InternalResult<?>> isInternalSuccess(Matcher<?> valueMatcher) {
        return isInternalResult(valueMatcher, list());
    }

    public static Matcher<InternalResult<?>> isInternalResult(Matcher<?> valueMatcher, Iterable<String> warnings) {
        return new FeatureMatcher<InternalResult<?>, Result<?>>(isResult(valueMatcher, warnings), "InternalResult as Result", "Result") {
            @Override
            protected Result<?> featureValueOf(InternalResult<?> actual) {
                return actual.toResult();
            }
        };
    }

    public static Matcher<Result<?>> isSuccess(Object value) {
        return isSuccess(deepEquals(value));
    }

    public static Matcher<Result<?>> isSuccess(Matcher<?> valueMatcher) {
        return isResult(valueMatcher, list());
    }

    public static Matcher<Result<?>> isResult(Matcher<?> valueMatcher, Iterable<String> warnings) {
        return Matchers.allOf(
            hasProperty("value", valueMatcher),
            hasWarnings(warnings));
    }

    public static Matcher<Object> hasWarnings(Iterable<String> warnings) {
        return hasProperty("warnings", containsInAnyOrder(eagerMap(warnings, Matchers::equalTo)));
    }
}
