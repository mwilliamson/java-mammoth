package org.zwobble.mammoth.tests.styles.parsing;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.styles.*;
import org.zwobble.mammoth.internal.styles.parsing.DocumentMatcherParser;
import org.zwobble.mammoth.internal.styles.parsing.StyleMappingTokeniser;
import org.zwobble.mammoth.internal.styles.parsing.TokenIterator;
import org.zwobble.mammoth.internal.styles.parsing.TokenType;

import java.util.Optional;

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

    @Test
    public void readsBold() {
        assertThat(
            parseDocumentMatcher("b"),
            deepEquals(new StyleMapBuilder().bold(HTML_PATH).build())
        );
    }

    @Test
    public void readsItalic() {
        assertThat(
            parseDocumentMatcher("i"),
            deepEquals(new StyleMapBuilder().italic(HTML_PATH).build())
        );
    }

    @Test
    public void readsUnderline() {
        assertThat(
            parseDocumentMatcher("u"),
            deepEquals(new StyleMapBuilder().underline(HTML_PATH).build())
        );
    }

    @Test
    public void readsStrikethrough() {
        assertThat(
            parseDocumentMatcher("strike"),
            deepEquals(new StyleMapBuilder().strikethrough(HTML_PATH).build())
        );
    }

    @Test
    public void readsAllCaps() {
        assertThat(
            parseDocumentMatcher("all-caps"),
            deepEquals(new StyleMapBuilder().allCaps(HTML_PATH).build())
        );
    }

    @Test
    public void readsSmallCaps() {
        assertThat(
            parseDocumentMatcher("small-caps"),
            deepEquals(new StyleMapBuilder().smallCaps(HTML_PATH).build())
        );
    }

    @Test
    public void readsHighlightWithoutColor() {
        assertThat(
            parseDocumentMatcher("highlight"),
            deepEquals(
                new StyleMapBuilder()
                    .mapHighlight(new HighlightMatcher(Optional.empty()), HTML_PATH)
                    .build()
            )
        );
    }

    @Test
    public void readsHighlightWithColor() {
        assertThat(
            parseDocumentMatcher("highlight[color='yellow']"),
            deepEquals(
                new StyleMapBuilder()
                    .mapHighlight(new HighlightMatcher(Optional.of("yellow")), HTML_PATH)
                    .build()
            )
        );
    }

    @Test
    public void readsCommentReference() {
        assertThat(
            parseDocumentMatcher("comment-reference"),
            deepEquals(new StyleMapBuilder().commentReference(HTML_PATH).build())
        );
    }

    @Test
    public void readsLineBreaks() {
        assertThat(
            parseDocumentMatcher("br[type='line']"),
            deepEquals(
                new StyleMapBuilder()
                    .mapBreak(BreakMatcher.LINE_BREAK, HTML_PATH)
                    .build()
            )
        );
    }

    @Test
    public void readsPageBreaks() {
        assertThat(
            parseDocumentMatcher("br[type='page']"),
            deepEquals(
                new StyleMapBuilder()
                    .mapBreak(BreakMatcher.PAGE_BREAK, HTML_PATH)
                    .build()
            )
        );
    }

    @Test
    public void readsColumnBreaks() {
        assertThat(
            parseDocumentMatcher("br[type='column']"),
            deepEquals(
                new StyleMapBuilder()
                    .mapBreak(BreakMatcher.COLUMN_BREAK, HTML_PATH)
                    .build()
            )
        );
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
