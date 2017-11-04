package org.zwobble.mammoth.tests.styles.parsing;

import org.junit.Test;
import org.zwobble.mammoth.internal.styles.*;
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
    public void readsPlainRun() {
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

    @Test
    public void readsPlainTable() {
        assertThat(
            parseTableMatcher("table"),
            deepEquals(TableMatcher.ANY));
    }

    @Test
    public void readsTableWithStyleId() {
        assertThat(
            parseTableMatcher("table.TableNormal"),
            deepEquals(TableMatcher.styleId("TableNormal")));
    }

    @Test
    public void readsTableWithStyleName() {
        assertThat(
            parseTableMatcher("table[style-name='Normal Table']"),
            deepEquals(TableMatcher.styleName("Normal Table")));
    }

    private ParagraphMatcher parseParagraphMatcher(String input) {
        TokenIterator<TokenType> tokens = StyleMappingTokeniser.tokenise(input);
        tokens.skip(TokenType.IDENTIFIER, "p");
        return DocumentMatcherParser.parseParagraphMatcher(tokens);
    }

    private RunMatcher parseRunMatcher(String input) {
        TokenIterator<TokenType> tokens = StyleMappingTokeniser.tokenise(input);
        tokens.skip(TokenType.IDENTIFIER, "r");
        return DocumentMatcherParser.parseRunMatcher(tokens);
    }

    private TableMatcher parseTableMatcher(String input) {
        TokenIterator<TokenType> tokens = StyleMappingTokeniser.tokenise(input);
        tokens.skip(TokenType.IDENTIFIER, "table");
        return DocumentMatcherParser.parseTableMatcher(tokens);
    }
}
