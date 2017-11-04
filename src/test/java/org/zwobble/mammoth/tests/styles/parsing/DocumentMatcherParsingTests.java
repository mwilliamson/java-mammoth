package org.zwobble.mammoth.tests.styles.parsing;

import org.hamcrest.Matcher;
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
            parseDocumentMatcher("p"),
            hasParagraphMatcher(ParagraphMatcher.ANY)
        );
    }

    @Test
    public void readsParagraphWithStyleId() {
        assertThat(
            parseDocumentMatcher("p.Heading1"),
            hasParagraphMatcher(ParagraphMatcher.styleId("Heading1")));
    }

    @Test
    public void readsParagraphWithExactStyleName() {
        assertThat(
            parseDocumentMatcher("p[style-name='Heading 1']"),
            hasParagraphMatcher(ParagraphMatcher.styleName(new EqualToStringMatcher("Heading 1")))
        );
    }

    @Test
    public void readsParagraphWithStyleNamePrefix() {
        assertThat(
            parseDocumentMatcher("p[style-name^='Heading']"),
            hasParagraphMatcher(ParagraphMatcher.styleName(new StartsWithStringMatcher("Heading")))
        );
    }

    @Test
    public void readsParagraphOrderedList() {
        assertThat(
            parseDocumentMatcher("p:ordered-list(2)"),
            hasParagraphMatcher(ParagraphMatcher.orderedList("1")));
    }

    @Test
    public void readsParagraphUnorderedList() {
        assertThat(
            parseDocumentMatcher("p:unordered-list(2)"),
            hasParagraphMatcher(ParagraphMatcher.unorderedList("1")));
    }

    @Test
    public void readsPlainRun() {
        assertThat(
            parseDocumentMatcher("r"),
            hasRunMatcher(RunMatcher.ANY));
    }

    @Test
    public void readsRunWithStyleId() {
        assertThat(
            parseDocumentMatcher("r.Heading1Char"),
            hasRunMatcher(RunMatcher.styleId("Heading1Char")));
    }

    @Test
    public void readsRunWithStyleName() {
        assertThat(
            parseDocumentMatcher("r[style-name='Heading 1 Char']"),
            hasRunMatcher(RunMatcher.styleName("Heading 1 Char")));
    }

    @Test
    public void readsPlainTable() {
        assertThat(
            parseDocumentMatcher("table"),
            hasTableMatcher(TableMatcher.ANY));
    }

    @Test
    public void readsTableWithStyleId() {
        assertThat(
            parseDocumentMatcher("table.TableNormal"),
            hasTableMatcher(TableMatcher.styleId("TableNormal")));
    }

    @Test
    public void readsTableWithStyleName() {
        assertThat(
            parseDocumentMatcher("table[style-name='Normal Table']"),
            hasTableMatcher(TableMatcher.styleName("Normal Table")));
    }

    private static final HtmlPath HTML_PATH = HtmlPath.element("placeholder");

    private StyleMap parseDocumentMatcher(String input) {
        TokenIterator<TokenType> tokens = StyleMappingTokeniser.tokenise(input);
        StyleMapBuilder builder = new StyleMapBuilder();
        DocumentMatcherParser.parse(tokens).accept(builder, HTML_PATH);
        return builder.build();
    }

    private Matcher<StyleMap> hasParagraphMatcher(ParagraphMatcher matcher) {
        return deepEquals(new StyleMapBuilder().mapParagraph(matcher, HTML_PATH).build());
    }

    private Matcher<StyleMap> hasRunMatcher(RunMatcher matcher) {
        return deepEquals(new StyleMapBuilder().mapRun(matcher, HTML_PATH).build());
    }

    private Matcher<StyleMap> hasTableMatcher(TableMatcher matcher) {
        return deepEquals(new StyleMapBuilder().mapTable(matcher, HTML_PATH).build());
    }
}
