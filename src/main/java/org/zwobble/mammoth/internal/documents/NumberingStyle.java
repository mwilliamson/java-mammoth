package org.zwobble.mammoth.internal.documents;

import java.util.Optional;

public class NumberingStyle {
    private final Optional<String> numId;

    public NumberingStyle(Optional<String> numId) {
        this.numId = numId;
    }

    public Optional<String> getNumId() {
        return numId;
    }
}
