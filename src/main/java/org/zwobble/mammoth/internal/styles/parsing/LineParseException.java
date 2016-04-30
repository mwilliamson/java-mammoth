package org.zwobble.mammoth.internal.styles.parsing;

class LineParseException extends RuntimeException {
    private final Token token;

    LineParseException(Token token, String message) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
