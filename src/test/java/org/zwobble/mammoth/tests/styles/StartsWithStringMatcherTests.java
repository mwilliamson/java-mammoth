package org.zwobble.mammoth.tests.styles;

import org.junit.Test;
import org.zwobble.mammoth.internal.styles.StartsWithStringMatcher;
import org.zwobble.mammoth.internal.styles.StringMatcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class StartsWithStringMatcherTests {
    @Test
    public void matchesStringWithPrefix() {
        StringMatcher matcher = new StartsWithStringMatcher("Heading");
        assertThat(matcher.matches("Heading"), equalTo(true));
        assertThat(matcher.matches("Heading 1"), equalTo(true));
        assertThat(matcher.matches("Custom Heading"), equalTo(false));
        assertThat(matcher.matches("Head"), equalTo(false));
        assertThat(matcher.matches("Header 2"), equalTo(false));
    }

    @Test
    public void isCaseInsensitive() {
        StringMatcher matcher = new StartsWithStringMatcher("Heading");
        assertThat(matcher.matches("heaDING"), equalTo(true));
    }
}
