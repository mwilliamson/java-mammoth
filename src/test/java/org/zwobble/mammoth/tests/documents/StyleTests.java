package org.zwobble.mammoth.tests.documents;

import org.junit.Test;
import org.zwobble.mammoth.internal.documents.Style;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class StyleTests {
    @Test
    public void descriptionOfStyleIncludesBothIdAndNameIfPresent() {
        Style style = new Style("Heading1", Optional.of("Heading 1"));
        assertEquals("Heading 1 (Style ID: Heading1)", style.describe());
    }

    @Test
    public void descriptionOfStyleIsJustStyleIdIfStyleNameIsMissing() {
        Style style = new Style("Heading1", Optional.empty());
        assertEquals("Style ID: Heading1", style.describe());
    }
}
