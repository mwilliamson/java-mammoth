package org.zwobble.mammoth.internal.styles.parsing;

public class Token<T> {
    private final int characterIndex;
    private final T tokenType;
    private final String value;

    public Token(int characterIndex, T tokenType, String value) {
        this.characterIndex = characterIndex;
        this.tokenType = tokenType;
        this.value = value;
    }

    public int getCharacterIndex() {
        return characterIndex;
    }

    public T getTokenType() {
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
