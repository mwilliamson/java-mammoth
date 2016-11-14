package org.zwobble.mammoth.internal.styles.parsing;

import java.util.List;

public class TokenIterator<T> {
    private final List<Token<T>> tokens;
    private int index;

    public TokenIterator(List<Token<T>> tokens) {
        this.tokens = tokens;
        this.index = 0;
    }

    public T peekTokenType() {
        return tokens.get(index).getTokenType();
    }

    public T peekTokenType(int count) {
        return tokens.get(index + count).getTokenType();
    }

    public Token<T> next(T type) {
        Token<T> token = tokens.get(index);
        if (token.getTokenType() == type) {
            index += 1;
            return token;
        } else {
            throw unexpectedTokenType(type, token);
        }
    }

    public String nextValue(T type) {
        return next(type).getValue();
    }

    public void skip() {
        index += 1;
    }

    public void skip(T tokenType) {
        Token token = tokens.get(index);
        if (token.getTokenType() != tokenType) {
            throw unexpectedTokenType(tokenType, token);
        }
        index += 1;
    }

    public void skip(T tokenType, String tokenValue) {
        Token<T> token = tokens.get(index);
        if (token.getTokenType() != tokenType) {
            throw unexpectedTokenType(tokenType, token);
        }
        String actualValue = token.getValue();
        if (!actualValue.equals(tokenValue)) {
            throw new LineParseException(token, "expected " + tokenType + " token with value " + tokenValue + " but value was " + actualValue);
        }
        index += 1;
    }

    private LineParseException unexpectedTokenType(T expected, Token<T> actual) {
        return new LineParseException(actual, "expected token of type " + expected + " but was of type " + actual.getTokenType());
    }
}
