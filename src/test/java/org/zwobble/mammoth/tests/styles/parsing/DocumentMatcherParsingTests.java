package org.zwobble.mammoth.tests.styles.parsing;

import org.junit.Test;
import org.zwobble.mammoth.internal.styles.EqualToStringMatcher;
import org.zwobble.mammoth.internal.styles.ParagraphMatcher;
import org.zwobble.mammoth.internal.styles.RunMatcher;
import org.zwobble.mammoth.internal.styles.StartsWithStringMatcher;
import org.zwobble.mammoth.internal.styles.parsing.DocumentMatcherParser;
import org.zwobble.mammoth.internal.styles.parsing.StyleMappingTokeniser;
import org.zwobble.mammoth.internal.styles.parsing.TokenIterator;
import org.zwobble.mammoth.internal.styles.parsing.TokenType;

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
    public void readsParagraphWithExactStyleName() {
        assertThat(
            parseParagraphMatcher("p[style-name='Heading 1']"),
            deepEquals(ParagraphMatcher.styleName(new EqualToStringMatcher("Heading 1")))
        );
    }

    @Test
    public void readsParagraphWithStyleNamePrefix() {
        assertThat(
            parseParagraphMatcher("p[style-name^='Heading']"),
            deepEquals(ParagraphMatcher.styleName(new StartsWithStringMatcher("Heading")))
        );
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
        TokenIterator tokens = StyleMappingTokeniser.tokenise(input);
        tokens.skip(TokenType.IDENTIFIER, "p");
        return DocumentMatcherParser.parseParagraphMatcher(tokens);
    }

    private RunMatcher parseRunMatcher(String input) {
        TokenIterator tokens = StyleMappingTokeniser.tokenise(input);
        tokens.skip(TokenType.IDENTIFIER, "r");
        return DocumentMatcherParser.parseRunMatcher(tokens);
    }
}
