package org.zwobble.mammoth.docx;

import java.util.Optional;

public class Style {
    private final Optional<String> name;

    public Style(Optional<String> name) {
        this.name = name;
    }

    public Optional<String> getName() {
        return name;
    }
}
