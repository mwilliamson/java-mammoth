package org.zwobble.mammoth.internal.styles.parsing;

public class EscapeSequences {
    public static String decode(String value) {
        return value.replaceAll("\\\\(.)", "$1");
    }
}
