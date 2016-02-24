package org.zwobble.mammoth.internal.util;

public class MammothStrings {
    public static String trimLeft(String value, char character) {
        int index = 0;
        while (index < value.length() && value.charAt(index) == character) {
            index++;
        }
        return value.substring(index);
    }
}
