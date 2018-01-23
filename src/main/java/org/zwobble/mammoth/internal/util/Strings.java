package org.zwobble.mammoth.internal.util;

import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class Strings {
    public static String trimLeft(String value, char character) {
        int index = 0;
        while (index < value.length() && value.charAt(index) == character) {
            index++;
        }
        return value.substring(index);
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static boolean startsWithIgnoreCase(String value, String prefix) {
        return value.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    public static List<String> split(String value, String separator) {
        return asList(value.split(Pattern.quote(separator)));
    }
}
