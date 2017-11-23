package org.zwobble.mammoth.tests.styles;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.documents.NumberingLevel;
import org.zwobble.mammoth.internal.documents.Paragraph;
import org.zwobble.mammoth.internal.documents.Style;
import org.zwobble.mammoth.internal.styles.ParagraphMatcher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.*;

public class ParagraphMatcherTests {
    @Test
    public void matcherWithoutConstraintsMatchesEverything() {
        assertTrue(ParagraphMatcher.ANY.matches(paragraph()));

        Paragraph paragraphWithStyleId = paragraph(withStyle(new Style("TipsParagraph", Optional.empty())));
        assertTrue(ParagraphMatcher.ANY.matches(paragraphWithStyleId));

        Paragraph paragraphWithStyleName = paragraph(withStyle(new Style("TipsParagraph", Optional.of("Tips Paragraph"))));
        assertTrue(ParagraphMatcher.ANY.matches(paragraphWithStyleName));
    }

    @Test
    public void matcherWithStyleIdOnlyMatchesParagraphsWithThatStyleId() {
        ParagraphMatcher matcher = ParagraphMatcher.styleId("TipsParagraph");
        assertFalse(matcher.matches(paragraph()));

        Paragraph paragraphWithCorrectStyleId = paragraph(
            withStyle(new Style("TipsParagraph", Optional.empty())));
        assertTrue(matcher.matches(paragraphWithCorrectStyleId));

        Paragraph paragraphWithIncorrectStyleId = paragraph(
            withStyle(new Style("Heading 1", Optional.empty())));
        assertFalse(matcher.matches(paragraphWithIncorrectStyleId));
    }

    @Test
    public void matcherWithStyleNameOnlyMatchesParagraphsWithThatStyleName() {
        ParagraphMatcher matcher = ParagraphMatcher.styleName("Tips Paragraph");
        assertFalse(matcher.matches(paragraph()));

        Paragraph paragraphWithNamelessStyle = paragraph(
            withStyle(new Style("TipsParagraph", Optional.empty())));
        assertFalse(matcher.matches(paragraphWithNamelessStyle));

        Paragraph paragraphWithCorrectStyleName = paragraph(
            withStyle(new Style("TipsParagraph", Optional.of("Tips Paragraph"))));
        assertTrue(matcher.matches(paragraphWithCorrectStyleName));

        Paragraph paragraphWithIncorrectStyleName = paragraph(
            withStyle(new Style("Heading 1", Optional.of("Heading 1"))));
        assertFalse(matcher.matches(paragraphWithIncorrectStyleName));
    }

    @Test
    public void styleNamesAreCaseInsensitive() {
        ParagraphMatcher matcher = ParagraphMatcher.styleName("tips paragraph");
        assertFalse(matcher.matches(paragraph()));

        Paragraph paragraphWithCorrectStyleName = paragraph(
            withStyle(new Style("TipsParagraph", Optional.of("Tips Paragraph"))));
        assertTrue(matcher.matches(paragraphWithCorrectStyleName));
    }

    @Test
    public void matcherWithOrderedListOnlyMatchesParagraphsWithOrderedListAtThatLeve() {
        ParagraphMatcher matcher = ParagraphMatcher.orderedList("4");
        assertFalse(matcher.matches(paragraph()));

        Paragraph paragraphWithCorrectNumbering = paragraph(
            withNumbering(NumberingLevel.ordered("4")));
        assertTrue(matcher.matches(paragraphWithCorrectNumbering));

        Paragraph paragraphWithIncorrectLevel = paragraph(
            withNumbering(NumberingLevel.ordered("3")));
        assertFalse(matcher.matches(paragraphWithIncorrectLevel));

        Paragraph paragraphWithIncorrectOrdering = paragraph(
            withNumbering(NumberingLevel.unordered("4")));
        assertFalse(matcher.matches(paragraphWithIncorrectOrdering));
    }

    @Test
    public void matcherWithUnorderedListOnlyMatchesParagraphsWithUnrderedListAtThatLeve() {
        ParagraphMatcher matcher = ParagraphMatcher.unorderedList("4");
        assertFalse(matcher.matches(paragraph()));

        Paragraph paragraphWithCorrectNumbering = paragraph(
            withNumbering(NumberingLevel.unordered("4")));
        assertTrue(matcher.matches(paragraphWithCorrectNumbering));

        Paragraph paragraphWithIncorrectLevel = paragraph(
            withNumbering(NumberingLevel.unordered("3")));
        assertFalse(matcher.matches(paragraphWithIncorrectLevel));

        Paragraph paragraphWithIncorrectOrdering = paragraph(
            withNumbering(NumberingLevel.ordered("4")));
        assertFalse(matcher.matches(paragraphWithIncorrectOrdering));
    }
}
