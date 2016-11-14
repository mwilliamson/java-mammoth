package org.zwobble.mammoth.internal.styles.parsing;

public class TokenParser {
    public static String parseClassName(TokenIterator<TokenType> tokens) {
        return EscapeSequences.decode(tokens.nextValue(TokenType.CLASS_NAME).substring(1));
    }

    public static String parseString(TokenIterator<TokenType> tokens) {
        String value = tokens.nextValue(TokenType.STRING);
        return EscapeSequences.decode(value.substring(1, value.length() - 1));
    }
}
