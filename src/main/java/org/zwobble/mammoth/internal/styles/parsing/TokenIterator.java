package org.zwobble.mammoth.internal.styles.parsing;

import java.util.List;

public class TokenIterator {
    private final List<Token> tokens;
    private int index;

    public TokenIterator(List<Token> tokens) {
        this.tokens = tokens;
        this.index = 0;
    }

    public TokenType peekTokenType() {
        return tokens.get(index).getTokenType();
    }

    public TokenType peekTokenType(int count) {
        return tokens.get(index + count).getTokenType();
    }

    public String nextValue(TokenType type) {
        Token token = tokens.get(index);
        if (token.getTokenType() == type) {
            index += 1;
            return token.getValue();
        } else {
            throw unexpectedTokenType(type, token.getTokenType());
        }
    }

    public void skip() {
        index += 1;
    }

    public void skip(TokenType tokenType) {
        if (peekTokenType() != tokenType) {
            throw unexpectedTokenType(tokenType, peekTokenType());
        }
        index += 1;
    }

    public void skip(TokenType tokenType, String tokenValue) {
        if (peekTokenType() != tokenType) {
            throw unexpectedTokenType(tokenType, peekTokenType());
        }
        String actualValue = tokens.get(index).getValue();
        if (!actualValue.equals(tokenValue)) {
            throw new IllegalArgumentException("expected " + tokenType + " token with value " + tokenValue + " but value was " + actualValue);
        }
        index += 1;
    }

    private IllegalArgumentException unexpectedTokenType(TokenType expected, TokenType actual) {
        return new IllegalArgumentException("expected token of type " + expected + " but was of type " + actual);
    }
}
