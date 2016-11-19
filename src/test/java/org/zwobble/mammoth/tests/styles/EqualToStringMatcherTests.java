package org.zwobble.mammoth.tests.styles;

import org.junit.Test;
import org.zwobble.mammoth.internal.styles.EqualToStringMatcher;
import org.zwobble.mammoth.internal.styles.StringMatcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EqualToStringMatcherTests {
    @Test
    public void isCaseInsensitive() {
        StringMatcher matcher = new EqualToStringMatcher("Heading 1");
        assertThat(matcher.matches("heaDING 1"), equalTo(true));
        assertThat(matcher.matches("heaDING 2"), equalTo(false));
    }
}
