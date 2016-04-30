package org.zwobble.mammoth.internal.styles.parsing;

public class ParseException extends RuntimeException {
    public ParseException(int lineNumber, Token token, String message) {
        super("error reading style map at line " + lineNumber + ", character " + (token.getCharacterIndex() + 1) + ": " + message);
    }
}
