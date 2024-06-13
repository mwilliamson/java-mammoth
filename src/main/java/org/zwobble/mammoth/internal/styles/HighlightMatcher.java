package org.zwobble.mammoth.internal.styles;

import java.util.Optional;

public class HighlightMatcher implements DocumentElementMatcher<String> {
    private final Optional<String> color;

    public HighlightMatcher(Optional<String> color) {
        this.color = color;
    }

    @Override
    public boolean matches(String color) {
        return !this.color.isPresent() || this.color.get().equals(color);
    }
}
