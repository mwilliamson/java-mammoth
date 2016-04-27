package org.zwobble.mammoth.internal.styles.parsing;

public class Token {
    private final TokenType tokenType;
    private final String value;

    public Token(TokenType tokenType, String value) {
        this.tokenType = tokenType;
        this.value = value;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Token(tokenType=" + tokenType + ", value=" + value + ")";
    }
}
