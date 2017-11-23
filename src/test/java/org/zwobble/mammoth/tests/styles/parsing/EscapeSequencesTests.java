package org.zwobble.mammoth.tests.styles.parsing;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.styles.parsing.EscapeSequences;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EscapeSequencesTests {
    @Test
    public void lineFeedsAreDecoded() {
        assertEquals("\n", EscapeSequences.decode("\\n"));
    }

    @Test
    public void carriageReturnsAreDecoded() {
        assertEquals("\r", EscapeSequences.decode("\\r"));
    }

    @Test
    public void tabsAreDecoded() {
        assertEquals("\t", EscapeSequences.decode("\\t"));
    }

    @Test
    public void backslashesAreDecoded() {
        assertEquals("\\", EscapeSequences.decode("\\\\"));
    }

    @Test
    public void colonsAreDecoded() {
        assertEquals(":", EscapeSequences.decode("\\:"));
    }
}
