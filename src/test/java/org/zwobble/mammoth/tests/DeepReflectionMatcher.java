package org.zwobble.mammoth.tests;

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
import java.util.Set;

import static java.util.Arrays.asList;
import static org.zwobble.mammoth.internal.util.MammothIterables.any;
import static org.zwobble.mammoth.internal.util.MammothIterables.lazyMap;
import static org.zwobble.mammoth.internal.util.MammothLists.eagerFilter;
import static org.zwobble.mammoth.internal.util.MammothLists.skip;

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

    private static boolean deepEquals(Object expected, Object actual) {
        Description description = new Description.NullDescription();
        return matchesSafely("", expected, actual, description);
    }

    private static <T> boolean matchesSafely(String path, T expected, T actual, Description mismatchDescription) {
        if (expected instanceof List && actual instanceof List) {
            return matchesList(path, (List)expected, (List)actual, mismatchDescription);
        }

        if (expected instanceof Set && actual instanceof Set) {
            return matchesSet(path, (Set)expected, (Set)actual, mismatchDescription);
        }

        if (expected instanceof Map && actual instanceof Map) {
            return matchesMap(path, (Map)expected, (Map)actual, mismatchDescription);
        }

        if (!expected.getClass().equals(actual.getClass())) {
            appendPath(mismatchDescription, path);
            mismatchDescription.appendText("was " + actual.getClass().getName());
            return false;
        }
        if (expected instanceof Optional && actual instanceof Optional) {
            return matchesOptional(path, (Optional)expected, (Optional)actual, mismatchDescription);
        }

        if (expected instanceof String || expected instanceof Boolean || expected instanceof Enum) {
            return matchesPrimitive(path, expected, actual, mismatchDescription);
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
                indentedList(lazyMap(skip(actual, expected.size()), DeepReflectionMatcher::describeValue)));
            return false;
        }

        if (actual.size() < expected.size()) {
            appendPath(mismatchDescription, path);
            mismatchDescription.appendText("missing elements:" +
                indentedList(lazyMap(skip(expected, actual.size()), DeepReflectionMatcher::describeValue)));
            return false;
        }

        for (int index = 0; index < expected.size(); index++) {
            if (!matchesSafely(path + "[" + index + "]", expected.get(index), actual.get(index), mismatchDescription)) {
                return false;
            }
        }

        return true;
    }

    private static <T> boolean matchesSet(String path, Set<?> expected, Set<?> actual, Description mismatchDescription) {
        List<?> missing = eagerFilter(
            expected,
            expectedElement -> !any(actual, actualElement -> deepEquals(expectedElement, actualElement)));

        if (!missing.isEmpty()) {
            appendPath(mismatchDescription, path);
            mismatchDescription.appendText("missing elements:" +
                indentedList(lazyMap(missing, DeepReflectionMatcher::describeValue)));
            return false;
        }

        List<?> extra = eagerFilter(
            actual,
            actualElement -> !any(expected, expectedElement -> deepEquals(expectedElement, actualElement)));

        if (!extra.isEmpty()) {
            appendPath(mismatchDescription, path);
            mismatchDescription.appendText("extra elements:" +
                indentedList(lazyMap(extra, DeepReflectionMatcher::describeValue)));
            return false;
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
                indentedList(lazyMap(
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

    private static <T> boolean matchesPrimitive(String path, T expected, T actual, Description mismatchDescription) {
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
        if (value instanceof String || value instanceof Boolean) {
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
            return "[" + indentedList(lazyMap(list, DeepReflectionMatcher::describeValue)) + "]";
        } else if (value instanceof Set) {
            Set<?> list = (Set)value;
            return "{" + indentedList(lazyMap(list, DeepReflectionMatcher::describeValue)) + "}";
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map)value;
            String entries = indentedList(
                lazyMap(
                    map.entrySet(),
                    entry -> describeValue(entry.getKey()) + "=" + describeValue(entry.getValue())));
            return "{" + entries + "}";
        } else {
            Class<?> clazz = value.getClass();
            List<Field> fields = fields(clazz);
            Iterable<String> fieldStrings = lazyMap(
                fields,
                field -> field.getName() + "=" + describeValue(readField(value, field)));
            return String.format("%s(%s)", clazz.getSimpleName(), indentedList(fieldStrings));
        }
    }

    private static String indentedList(Iterable<String> values) {
        return String.join(",", lazyMap(values, value -> "\n  " + indent(value)));
    }

    private static String indent(String value) {
        return value.replaceAll("\n", "\n  ");
    }

    private static List<Field> fields(Class<?> clazz) {
        return eagerFilter(
            asList(clazz.getDeclaredFields()),
            field -> !Modifier.isStatic(field.getModifiers()));
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
