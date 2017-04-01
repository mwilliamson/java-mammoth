package org.zwobble.mammoth.internal.styles;

import org.zwobble.mammoth.internal.util.Strings;

public class StartsWithStringMatcher implements StringMatcher {
    private final String prefix;

    public StartsWithStringMatcher(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean matches(String value) {
        return Strings.startsWithIgnoreCase(value, prefix);
    }
}
