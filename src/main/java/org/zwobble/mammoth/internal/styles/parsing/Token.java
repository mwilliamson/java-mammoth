package org.zwobble.mammoth.internal.styles.parsing;

public class Token {
    private final int characterIndex;
    private final TokenType tokenType;
    private final String value;

    public Token(int characterIndex, TokenType tokenType, String value) {
        this.characterIndex = characterIndex;
        this.tokenType = tokenType;
        this.value = value;
    }

    public int getCharacterIndex() {
        return characterIndex;
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
