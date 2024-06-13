package org.zwobble.mammoth.tests.styles;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.styles.HighlightMatcher;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class HighlightMatcherTests {
    @Test
    public void highlightMatcherWithoutColorMatchesAllHighlightElements() {
        HighlightMatcher matcher = new HighlightMatcher(Optional.empty());

        boolean result = matcher.matches("yellow");

        assertThat(result, equalTo(true));
    }

    @Test
    public void highlightMatcherWithColorMatchesHighlightWithThatColor() {
        HighlightMatcher matcher = new HighlightMatcher(Optional.of("yellow"));

        boolean result = matcher.matches("yellow");

        assertThat(result, equalTo(true));
    }

    @Test
    public void highlightMatcherWithColorDoesNotMatchHighlightsWithOtherColors() {
        HighlightMatcher matcher = new HighlightMatcher(Optional.of("yellow"));

        boolean result = matcher.matches("red");

        assertThat(result, equalTo(false));
    }
}
