package org.zwobble.mammoth.internal.results;

public class Warning {
    public static Warning warning(String message) {
        return new Warning(message);
    }
    private final String message;

    private Warning(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
