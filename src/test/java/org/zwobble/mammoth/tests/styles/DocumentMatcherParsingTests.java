package org.zwobble.mammoth.tests.styles;

import org.junit.Test;
import org.parboiled.support.Var;
import org.zwobble.mammoth.styles.ParagraphMatcher;
import org.zwobble.mammoth.styles.RunMatcher;
import org.zwobble.mammoth.styles.parsing.Parsing;
import org.zwobble.mammoth.styles.parsing.StyleMappingParser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;

public class DocumentMatcherParsingTests {
    @Test
    public void readsPlainParagraph() {
        assertThat(
            parseParagraphMatcher("p"),
            deepEquals(ParagraphMatcher.ANY));
    }

    @Test
    public void readsParagraphWithStyleId() {
        assertThat(
            parseParagraphMatcher("p.Heading1"),
            deepEquals(ParagraphMatcher.styleId("Heading1")));
    }

    @Test
    public void readsParagraphWithStyleName() {
        assertThat(
            parseParagraphMatcher("p[style-name='Heading 1']"),
            deepEquals(ParagraphMatcher.styleName("Heading 1")));
    }

    @Test
    public void readsParagraphOrderedList() {
        assertThat(
            parseParagraphMatcher("p:ordered-list(2)"),
            deepEquals(ParagraphMatcher.orderedList("1")));
    }

    @Test
    public void readsParagraphUnorderedList() {
        assertThat(
            parseParagraphMatcher("p:unordered-list(2)"),
            deepEquals(ParagraphMatcher.unorderedList("1")));
    }
    @Test
    public void readsRunParagraph() {
        assertThat(
            parseRunMatcher("r"),
            deepEquals(RunMatcher.ANY));
    }

    @Test
    public void readsRunWithStyleId() {
        assertThat(
            parseRunMatcher("r.Heading1Char"),
            deepEquals(RunMatcher.styleId("Heading1Char")));
    }

    @Test
    public void readsRunWithStyleName() {
        assertThat(
            parseRunMatcher("r[style-name='Heading 1 Char']"),
            deepEquals(RunMatcher.styleName("Heading 1 Char")));
    }

    private ParagraphMatcher parseParagraphMatcher(String input) {
        Var<ParagraphMatcher> matcher = new Var<>();
        Parsing.parse(StyleMappingParser.class, parser -> parser.ParagraphMatcher(matcher), input);
        return matcher.get();
    }

    private RunMatcher parseRunMatcher(String input) {
        Var<RunMatcher> matcher = new Var<>();
        Parsing.parse(StyleMappingParser.class, parser -> parser.RunMatcher(matcher), input);
        return matcher.get();
    }
}
