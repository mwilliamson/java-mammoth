package org.zwobble.mammoth.internal.styles;

public class EqualToStringMatcher implements StringMatcher {
    private final String value;

    public EqualToStringMatcher(String value) {
        this.value = value;
    }

    @Override
    public boolean matches(String value) {
        return this.value.equalsIgnoreCase(value);
    }
}
