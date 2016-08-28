package org.zwobble.mammoth.tests.styles.parsing;

import org.junit.Test;
import org.zwobble.mammoth.internal.styles.HtmlPath;
import org.zwobble.mammoth.internal.styles.ParagraphMatcher;
import org.zwobble.mammoth.internal.styles.RunMatcher;
import org.zwobble.mammoth.internal.styles.StyleMap;
import org.zwobble.mammoth.internal.styles.parsing.StyleMapParser;

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
        assertThat(styleMap, deepEquals(StyleMap.builder().mapParagraph(ParagraphMatcher.ANY, HtmlPath.collapsibleElement("p")).build()));
    }

    @Test
    public void canMapRuns() {
        StyleMap styleMap = StyleMapParser.parse("r => p");
        assertThat(styleMap, deepEquals(StyleMap.builder().mapRun(RunMatcher.ANY, HtmlPath.collapsibleElement("p")).build()));
    }

    @Test
    public void canMapBold() {
        StyleMap styleMap = StyleMapParser.parse("b => em");
        assertThat(styleMap, deepEquals(StyleMap.builder().bold(HtmlPath.collapsibleElement("em")).build()));
    }

    @Test
    public void canMapItalic() {
        StyleMap styleMap = StyleMapParser.parse("i => strong");
        assertThat(styleMap, deepEquals(StyleMap.builder().italic(HtmlPath.collapsibleElement("strong")).build()));
    }

    @Test
    public void canMapUnderline() {
        StyleMap styleMap = StyleMapParser.parse("u => em");
        assertThat(styleMap, deepEquals(StyleMap.builder().underline(HtmlPath.collapsibleElement("em")).build()));
    }

    @Test
    public void canMapStrikethrough() {
        StyleMap styleMap = StyleMapParser.parse("strike => del");
        assertThat(styleMap, deepEquals(StyleMap.builder().strikethrough(HtmlPath.collapsibleElement("del")).build()));
    }

    @Test
    public void canMapCommentReference() {
        StyleMap styleMap = StyleMapParser.parse("comment-reference =>");
        assertThat(styleMap, deepEquals(StyleMap.builder().commentReference(HtmlPath.EMPTY).build()));
    }

    @Test
    public void blankLinesAreIgnored() {
        StyleMap styleMap = StyleMapParser.parse("\n\n  \n\np =>\n\r\n");
        assertThat(styleMap, deepEquals(StyleMap.builder().mapParagraph(ParagraphMatcher.ANY, HtmlPath.EMPTY).build()));
    }

    @Test
    public void lineStartingWithHashIsIgnored() {
        StyleMap styleMap = StyleMapParser.parse("#p => p");
        assertThat(styleMap, deepEquals(StyleMap.EMPTY));
    }

    @Test
    public void canParseMultipleMappings() {
        StyleMap styleMap = StyleMapParser.parse("p =>\nr =>");
        assertThat(styleMap, deepEquals(StyleMap.builder()
            .mapParagraph(ParagraphMatcher.ANY, HtmlPath.EMPTY)
            .mapRun(RunMatcher.ANY, HtmlPath.EMPTY)
            .build()));
    }
}
