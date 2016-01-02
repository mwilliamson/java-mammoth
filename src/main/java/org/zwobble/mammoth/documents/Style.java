package org.zwobble.mammoth.documents;

import java.util.Optional;

public class Style {
    private final String styleId;
    private final Optional<String> name;

    public Style(String styleId, Optional<String> name) {
        this.styleId = styleId;
        this.name = name;
    }

    public String getStyleId() {
        return styleId;
    }

    public Optional<String> getName() {
        return name;
    }
}
