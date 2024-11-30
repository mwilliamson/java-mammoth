package org.zwobble.mammoth.tests.docx;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.zwobble.mammoth.internal.documents.*;
import org.zwobble.mammoth.tests.DeepReflectionMatcher;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;

public class DocumentMatchers {
    private DocumentMatchers() {}

    static Matcher<DocumentElement> isParagraph(Paragraph expected) {
        return new DeepReflectionMatcher<>(expected);
    }

    static Matcher<DocumentElement> isRun(Run expected) {
        return new DeepReflectionMatcher<>(expected);
    }

    static Matcher<DocumentElement> isTextElement(String value) {
        return new DeepReflectionMatcher<>(new Text(value));
    }

    @SafeVarargs
    static Matcher<HasChildren> hasChildren(Matcher<DocumentElement>... children) {
        Matcher<Iterable<? extends DocumentElement>> contains = children.length == 0 ? emptyIterable() : contains(asList(children));
        return hasProperty("children", contains);
    }

    @SafeVarargs
    static Matcher<DocumentElement> isParagraph(Matcher<? super Paragraph>... matchers) {
        return cast(Paragraph.class, allOf(matchers));
    }

    static Matcher<Paragraph> hasParagraphStyle(Matcher<Optional<Style>> style) {
        return hasProperty("style", style);
    }

    static Matcher<DocumentElement> isEmptyRun() {
        return isRun(hasChildren());
    }

    @SafeVarargs
    static Matcher<DocumentElement> isRun(Matcher<? super Run>... matchers) {
        return cast(Run.class, allOf(matchers));
    }

    @SafeVarargs
    static Matcher<DocumentElement> isTable(Matcher<? super Table>... matchers) {
        return cast(Table.class, allOf(matchers));
    }

    @SafeVarargs
    static Matcher<DocumentElement> isRow(Matcher<? super TableRow>... matchers) {
        return cast(TableRow.class, allOf(matchers));
    }

    static Matcher<TableRow> isHeader(boolean isHeader) {
        return hasProperty("header", equalTo(isHeader));
    }

    private static <T, U> Matcher<U> cast(Class<T> clazz, Matcher<? super T> downcastMatcher) {
        return new TypeSafeDiagnosingMatcher<U>() {
            @Override
            public void describeTo(Description description) {
                downcastMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(Object item, Description mismatchDescription) {
                if (!clazz.isInstance(item)) {
                    mismatchDescription.appendText("was a " + item.getClass().getSimpleName());
                    return false;
                } if (downcastMatcher.matches(item)) {
                    return true;
                } else {
                    downcastMatcher.describeMismatch(item, mismatchDescription);
                    return false;
                }
            }
        };
    }

    @SafeVarargs
    static Matcher<DocumentElement> isHyperlink(Matcher<? super Hyperlink>... matchers) {
        return cast(Hyperlink.class, allOf(matchers));
    }

    static Matcher<DocumentElement> isCheckbox() {
        return instanceOf(Checkbox.class);
    }

    static Matcher<DocumentElement> isCheckbox(boolean checked) {
        return new DeepReflectionMatcher<>(new Checkbox(checked));
    }

    static Matcher<Hyperlink> hasHref(String href) {
        return hasProperty("href", equalTo(Optional.of(href)));
    }

    static Matcher<Hyperlink> hasNoHref() {
        return hasProperty("href", equalTo(Optional.empty()));
    }

    static Matcher<Hyperlink> hasAnchor(String anchor) {
        return hasProperty("anchor", equalTo(Optional.of(anchor)));
    }

    static Matcher<Hyperlink> hasNoAnchor() {
        return hasProperty("anchor", equalTo(Optional.empty()));
    }

    static Matcher<Hyperlink> hasTargetFrame(String targetFrame) {
        return hasProperty("targetFrame", equalTo(Optional.of(targetFrame)));
    }

    static Matcher<Hyperlink> hasNoTargetFrame() {
        return hasProperty("targetFrame", equalTo(Optional.empty()));
    }
}
