package org.zwobble.mammoth.tests;

import org.hamcrest.Matcher;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

public class Matchers {
    private Matchers() {}

    public static <T> Matcher<Iterable<? extends T>> isSameSequence(Matcher<? super T>... matchers) {
        return Matchers.<T>isSameSequence(asList(matchers));
    }

    public static <T> Matcher<Iterable<? extends T>> isSameSequence(List<Matcher<? super T>> matchers) {
        return matchers.size() == 0 ? emptyIterable() : contains(matchers);
    }
}
