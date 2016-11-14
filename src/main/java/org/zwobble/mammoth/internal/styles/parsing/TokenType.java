package org.zwobble.mammoth.internal.styles.parsing;

public enum TokenType {
    WHITESPACE,
    IDENTIFIER,
    CLASS_NAME,
    COLON,
    GREATER_THAN,
    ARROW,
    EQUALS,
    OPEN_PAREN,
    CLOSE_PAREN,
    OPEN_SQUARE_BRACKET,
    CLOSE_SQUARE_BRACKET,
    STRING,
    UNTERMINATED_STRING,
    INTEGER,
    CHOICE,
    BANG,
    EOF,
    UNKNOWN
}
