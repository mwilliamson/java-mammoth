package org.zwobble.mammoth.internal.styles.parsing;

public class ParseException extends RuntimeException {
    public ParseException(String line, int lineNumber, Token token, String message) {
        super(generateMessage(line, lineNumber, token, message));
    }

    private static String generateMessage(String line, int lineNumber, Token token, String message) {
        return "error reading style map at line " + lineNumber + ", character " + (token.getCharacterIndex() + 1) +
            ": " + message + "\n\n" +
            line + "\n" +
            repeatString(" ", token.getCharacterIndex()) + "^";
    }

    private static String repeatString(String value, int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; i++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
