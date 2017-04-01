package org.zwobble.mammoth.internal.util;

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
}
