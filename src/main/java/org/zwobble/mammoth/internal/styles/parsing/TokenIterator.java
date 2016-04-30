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

    public Token next(TokenType type) {
        Token token = tokens.get(index);
        if (token.getTokenType() == type) {
            index += 1;
            return token;
        } else {
            throw unexpectedTokenType(type, token);
        }
    }

    public String nextValue(TokenType type) {
        return next(type).getValue();
    }

    public void skip() {
        index += 1;
    }

    public void skip(TokenType tokenType) {
        Token token = tokens.get(index);
        if (token.getTokenType() != tokenType) {
            throw unexpectedTokenType(tokenType, token);
        }
        index += 1;
    }

    public void skip(TokenType tokenType, String tokenValue) {
        Token token = tokens.get(index);
        if (token.getTokenType() != tokenType) {
            throw unexpectedTokenType(tokenType, token);
        }
        String actualValue = token.getValue();
        if (!actualValue.equals(tokenValue)) {
            throw new LineParseException(token, "expected " + tokenType + " token with value " + tokenValue + " but value was " + actualValue);
        }
        index += 1;
    }

    private LineParseException unexpectedTokenType(TokenType expected, Token actual) {
        return new LineParseException(actual, "expected token of type " + expected + " but was of type " + actual.getTokenType());
    }
}
