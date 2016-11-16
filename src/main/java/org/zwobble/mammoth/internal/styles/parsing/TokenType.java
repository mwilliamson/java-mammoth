package org.zwobble.mammoth.internal.styles.parsing;

public enum TokenType {
    WHITESPACE,
    IDENTIFIER,
    CLASS_NAME,
    SYMBOL,
    STRING,
    UNTERMINATED_STRING,
    INTEGER,
    EOF,
    UNKNOWN
}
