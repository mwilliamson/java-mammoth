package org.zwobble.mammoth.tests;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

public class DeepReflectionMatcher<T> extends TypeSafeDiagnosingMatcher<T> {
    public static <T> Matcher<T> deepEquals(T value) {
        return new DeepReflectionMatcher<>(value);
    }

    private final T expected;

    public DeepReflectionMatcher(T expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(T item, Description mismatchDescription) {
        return matchesSafely("", expected, item, mismatchDescription);
    }

    private static <T> boolean matchesSafely(String path, T expected, T actual, Description mismatchDescription) {
        if (expected instanceof List && actual instanceof List) {
            return matchesList(path, (List)expected, (List)actual, mismatchDescription);
        }

        if (expected instanceof Map && actual instanceof Map) {
            return matchesMap(path, (Map)expected, (Map)actual, mismatchDescription);
        }

        if (!expected.getClass().equals(actual.getClass())) {
            mismatchDescription.appendText("was " + actual.getClass().getName());
            return false;
        }
        if (expected instanceof Optional && actual instanceof Optional) {
            return matchesOptional(path, (Optional)expected, (Optional)actual, mismatchDescription);
        }

        if (expected instanceof String) {
            return matchesString(path, expected, actual, mismatchDescription);
        }

        for (Field field : fields(expected.getClass())) {
            if (!matchesSafely(path + "." + field.getName(), readField(expected, field), readField(actual, field), mismatchDescription)) {
                return false;
            }
        }

        return true;
    }

    private static <T> boolean matchesList(String path, List<?> expected, List<?> actual, Description mismatchDescription) {
        if (actual.size() > expected.size()) {
            appendPath(mismatchDescription, path);
            mismatchDescription.appendText("extra elements:" +
                indentedList(transform(Iterables.skip(actual, expected.size()), DeepReflectionMatcher::describeValue)));
            return false;
        }

        if (actual.size() < expected.size()) {
            appendPath(mismatchDescription, path);
            mismatchDescription.appendText("missing elements:" +
                indentedList(transform(Iterables.skip(expected, actual.size()), DeepReflectionMatcher::describeValue)));
            return false;
        }

        for (int index = 0; index < expected.size(); index++) {
            if (!matchesSafely(path + "[" + index + "]", expected.get(index), actual.get(index), mismatchDescription)) {
                return false;
            }
        }

        return true;
    }

    private static <T> boolean matchesMap(String path, Map<?, ?> expected, Map<?, ?> actual, Description mismatchDescription) {
        if (!handleExtraElements(path, expected, actual, mismatchDescription, "extra elements:")) {
            return false;
        }
        if (!handleExtraElements(path, actual, expected, mismatchDescription, "missing elements:")) {
            return false;
        }

        for (Object key : expected.keySet()) {
            if (!matchesSafely(path + "[" + key + "]", expected.get(key), actual.get(key), mismatchDescription)) {
                return false;
            }
        }

        return true;
    }

    private static boolean handleExtraElements(String path, Map<?, ?> expected, Map<?, ?> actual, Description mismatchDescription, String prefix) {
        Sets.SetView<?> extraElements = Sets.difference(actual.keySet(), expected.keySet());
        if (!extraElements.isEmpty()) {
            appendPath(mismatchDescription, path);
            mismatchDescription.appendText(prefix +
                indentedList(transform(
                    extraElements,
                    key -> describeValue(key) + "=" + describeValue(actual.get(key)))));
            return false;
        }
        return true;
    }

    private static boolean matchesOptional(String path, Optional expected, Optional actual, Description mismatchDescription) {
        if (actual.isPresent() && !expected.isPresent()) {
            appendPath(mismatchDescription, path);
            mismatchDescription.appendText("had value " + describeValue(actual.get()));
            return false;
        }

        if (expected.isPresent() && !actual.isPresent()) {
            appendPath(mismatchDescription, path);
            mismatchDescription.appendText("was empty");
            return false;
        }

        if (actual.isPresent() && expected.isPresent()) {
            return matchesSafely(path, expected.get(), actual.get(), mismatchDescription);
        }

        return true;
    }

    private static <T> boolean matchesString(String path, T expected, T actual, Description mismatchDescription) {
        Matcher<Object> matcher = Matchers.equalTo(expected);
        if (!matcher.matches(actual)) {
            appendPath(mismatchDescription, path);
            matcher.describeMismatch(actual, mismatchDescription);
            return false;
        } else {
            return true;
        }
    }

    private static void appendPath(Description mismatchDescription, String path) {
        mismatchDescription.appendText(path + ": ");
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(describeValue(expected));
    }

    private static String describeValue(Object value) {
        if (value instanceof String) {
            return value.toString();
        } else if (value instanceof Optional) {
            Optional<?> optional = (Optional)value;
            if (optional.isPresent()) {
                return describeValue(optional.get());
            } else {
                return "(empty)";
            }
        } else if (value instanceof List) {
            List<?> list = (List)value;
            return "[" + indentedList(transform(list, DeepReflectionMatcher::describeValue)) + "]";
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map)value;
            String entries = indentedList(
                transform(
                    map.entrySet(),
                    entry -> describeValue(entry.getKey()) + "=" + describeValue(entry.getValue())));
            return "{" + entries + "}";
        } else {
            Class<?> clazz = value.getClass();
            List<Field> fields = fields(clazz);
            Iterable<String> fieldStrings = transform(
                fields,
                field -> field.getName() + "=" + describeValue(readField(value, field)));
            return String.format("%s(%s)", clazz.getSimpleName(), indentedList(fieldStrings));
        }
    }

    private static String indentedList(Iterable<String> values) {
        return Joiner.on(",").join(transform(values, value -> "\n  " + indent(value)));
    }

    private static String indent(String value) {
        return value.replaceAll("\n", "\n  ");
    }

    private static ImmutableList<Field> fields(Class<?> clazz) {
        return ImmutableList.copyOf(filter(
            asList(clazz.getDeclaredFields()),
            field -> !Modifier.isStatic(field.getModifiers())));
    }

    private static Object readField(Object obj, Field field) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }
}
