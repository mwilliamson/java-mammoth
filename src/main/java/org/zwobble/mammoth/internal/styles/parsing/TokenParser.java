package org.zwobble.mammoth.internal.styles.parsing;

import java.util.Optional;

public class TokenParser {
    public static Optional<String> parseClassName(TokenIterator<TokenType> tokens) {
        if (tokens.trySkip(TokenType.SYMBOL, ".")) {
            return Optional.of(EscapeSequences.decode(tokens.nextValue(TokenType.IDENTIFIER)));
        } else {
            return Optional.empty();
        }
    }

    public static String parseString(TokenIterator<TokenType> tokens) {
        String value = tokens.nextValue(TokenType.STRING);
        return EscapeSequences.decode(value.substring(1, value.length() - 1));
    }
}
