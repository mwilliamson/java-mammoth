package org.zwobble.mammoth.internal.styles.parsing;

class LineParseException extends RuntimeException {
    static <T> LineParseException lineParseException(Token<T> token, String message) {
        return new LineParseException(token.getCharacterIndex(), message);
    }

    private final int characterIndex;

    private LineParseException(int characterIndex, String message) {
        super(message);
        this.characterIndex = characterIndex;
    }

    int getCharacterIndex() {
        return characterIndex;
    }
}
