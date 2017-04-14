package org.zwobble.mammoth.internal.styles.parsing;

import java.util.List;

import static org.zwobble.mammoth.internal.styles.parsing.LineParseException.lineParseException;

public class TokenIterator<T> {
    private final List<Token<T>> tokens;
    private final Token<T> end;
    private int index;

    public TokenIterator(List<Token<T>> tokens, Token<T> end) {
        this.tokens = tokens;
        this.end = end;
        this.index = 0;
    }

    public boolean isNext(int offset, T tokenType, String value) {
        int tokenIndex = index + offset;
        Token<T> token = getToken(tokenIndex);
        return token.getTokenType().equals(tokenType) && token.getValue().equals(value);
    }

    public boolean isNext(T tokenType, String value) {
        return isNext(0, tokenType, value);
    }

    public boolean trySkip(T tokenType, String value) {
        if (isNext(tokenType, value)) {
            skip();
            return true;
        } else {
            return false;
        }
    }

    public T peekTokenType() {
        return getToken(index).getTokenType();
    }

    public Token<T> next() {
        Token<T> token = getToken(index);
        index += 1;
        return token;
    }

    public Token<T> next(T type) {
        Token<T> token = getToken(index);
        if (token.getTokenType().equals(type)) {
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
        Token<T> token = getToken(index);
        if (!token.getTokenType().equals(tokenType)) {
            throw unexpectedTokenType(tokenType, token);
        }
        index += 1;
    }

    public void skip(T tokenType, String tokenValue) {
        Token<T> token = getToken(index);
        if (!token.getTokenType().equals(tokenType)) {
            throw unexpectedTokenType(tokenType, token);
        }
        String actualValue = token.getValue();
        if (!actualValue.equals(tokenValue)) {
            throw lineParseException(token, "expected " + tokenType + " token with value " + tokenValue + " but value was " + actualValue);
        }
        index += 1;
    }

    private LineParseException unexpectedTokenType(T expected, Token<T> actual) {
        return lineParseException(actual, "expected token of type " + expected + " but was of type " + actual.getTokenType());
    }

    public boolean tryParse(Action action) {
        int originalIndex = index;
        try {
            action.run();
            return true;
        }
        catch (LineParseException exception) {
            index = originalIndex;
            return false;
        }
    }

    private Token<T> getToken(int index) {
        if (index < tokens.size()) {
            return tokens.get(index);
        } else {
            return end;
        }
    }

    public interface Action {
        void run();
    }
}
