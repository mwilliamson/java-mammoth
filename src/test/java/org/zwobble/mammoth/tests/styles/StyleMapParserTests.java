package org.zwobble.mammoth.tests.styles;

import org.junit.Test;
import org.zwobble.mammoth.styles.HtmlPath;
import org.zwobble.mammoth.styles.ParagraphMatcher;
import org.zwobble.mammoth.styles.StyleMap;
import org.zwobble.mammoth.styles.parsing.StyleMapParser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;

public class StyleMapParserTests {
    @Test
    public void emptyStringIsParsedAsEmptyStyleMap() {
        StyleMap styleMap = StyleMapParser.parse("");
        assertThat(styleMap, deepEquals(StyleMap.builder().build()));
    }

    @Test
    public void canMapParagraphs() {
        StyleMap styleMap = StyleMapParser.parse("p => p");
        assertThat(styleMap, deepEquals(StyleMap.builder().mapParagraph(ParagraphMatcher.ANY, HtmlPath.EMPTY).build()));
    }
}
