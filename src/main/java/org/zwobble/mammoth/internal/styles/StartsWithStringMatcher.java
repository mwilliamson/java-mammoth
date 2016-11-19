package org.zwobble.mammoth.internal.styles;

public class StartsWithStringMatcher implements StringMatcher {
    private final String prefix;

    public StartsWithStringMatcher(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean matches(String value) {
        return value.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
