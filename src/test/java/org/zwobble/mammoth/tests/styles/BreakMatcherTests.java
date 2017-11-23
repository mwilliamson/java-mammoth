package org.zwobble.mammoth.tests.styles;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.documents.Break;
import org.zwobble.mammoth.internal.styles.BreakMatcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class BreakMatcherTests {
    @Test
    public void whenBreakHasDifferentTypeToBreakMatcherThenMatchFails() {
        assertThat(BreakMatcher.LINE_BREAK.matches(Break.PAGE_BREAK), equalTo(false));
        assertThat(BreakMatcher.PAGE_BREAK.matches(Break.LINE_BREAK), equalTo(false));
    }

    @Test
    public void whenBreakHasTheSameTypeAsBreakMatcherThenMatchSucceeds() {
        assertThat(BreakMatcher.LINE_BREAK.matches(Break.LINE_BREAK), equalTo(true));
        assertThat(BreakMatcher.PAGE_BREAK.matches(Break.PAGE_BREAK), equalTo(true));
    }
}
