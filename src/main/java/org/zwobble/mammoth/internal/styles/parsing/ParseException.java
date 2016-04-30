package org.zwobble.mammoth.internal.styles.parsing;

public class ParseException extends RuntimeException {
    public ParseException(Token token, String message) {
        super("error reading style map at character " + (token.getCharacterIndex() + 1) + ": " + message);
    }
}
