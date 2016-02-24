package org.zwobble.mammoth.tests.styles;

import org.junit.Test;
import org.zwobble.mammoth.internal.documents.NumberingLevel;
import org.zwobble.mammoth.internal.documents.Paragraph;
import org.zwobble.mammoth.internal.documents.Style;
import org.zwobble.mammoth.internal.styles.ParagraphMatcher;

import java.util.Optional;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.NUMBERING;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.PARAGRAPH;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.STYLE;

public class ParagraphMatcherTests {
    @Test
    public void matcherWithoutConstraintsMatchesEverything() {
        assertTrue(ParagraphMatcher.ANY.matches(make(a(PARAGRAPH))));

        Paragraph paragraphWithStyleId = make(a(PARAGRAPH,
            with(STYLE, Optional.of(new Style("TipsParagraph", Optional.empty())))));
        assertTrue(ParagraphMatcher.ANY.matches(paragraphWithStyleId));

        Paragraph paragraphWithStyleName = make(a(PARAGRAPH,
            with(STYLE, Optional.of(new Style("TipsParagraph", Optional.of("Tips Paragraph"))))));
        assertTrue(ParagraphMatcher.ANY.matches(paragraphWithStyleName));
    }

    @Test
    public void matcherWithStyleIdOnlyMatchesParagraphsWithThatStyleId() {
        ParagraphMatcher matcher = ParagraphMatcher.styleId("TipsParagraph");
        assertFalse(matcher.matches(make(a(PARAGRAPH))));

        Paragraph paragraphWithCorrectStyleId = make(a(PARAGRAPH,
            with(STYLE, Optional.of(new Style("TipsParagraph", Optional.empty())))));
        assertTrue(matcher.matches(paragraphWithCorrectStyleId));

        Paragraph paragraphWithIncorrectStyleId = make(a(PARAGRAPH,
            with(STYLE, Optional.of(new Style("Heading 1", Optional.empty())))));
        assertFalse(matcher.matches(paragraphWithIncorrectStyleId));
    }

    @Test
    public void matcherWithStyleNameOnlyMatchesParagraphsWithThatStyleName() {
        ParagraphMatcher matcher = ParagraphMatcher.styleName("Tips Paragraph");
        assertFalse(matcher.matches(make(a(PARAGRAPH))));

        Paragraph paragraphWithNamelessStyle = make(a(PARAGRAPH,
            with(STYLE, Optional.of(new Style("TipsParagraph", Optional.empty())))));
        assertFalse(matcher.matches(paragraphWithNamelessStyle));

        Paragraph paragraphWithCorrectStyleName = make(a(PARAGRAPH,
            with(STYLE, Optional.of(new Style("TipsParagraph", Optional.of("Tips Paragraph"))))));
        assertTrue(matcher.matches(paragraphWithCorrectStyleName));

        Paragraph paragraphWithIncorrectStyleName = make(a(PARAGRAPH,
            with(STYLE, Optional.of(new Style("Heading 1", Optional.of("Heading 1"))))));
        assertFalse(matcher.matches(paragraphWithIncorrectStyleName));
    }

    @Test
    public void styleNamesAreCaseInsensitive() {
        ParagraphMatcher matcher = ParagraphMatcher.styleName("tips paragraph");
        assertFalse(matcher.matches(make(a(PARAGRAPH))));

        Paragraph paragraphWithCorrectStyleName = make(a(PARAGRAPH,
            with(STYLE, Optional.of(new Style("TipsParagraph", Optional.of("Tips Paragraph"))))));
        assertTrue(matcher.matches(paragraphWithCorrectStyleName));
    }

    @Test
    public void matcherWithOrderedListOnlyMatchesParagraphsWithOrderedListAtThatLeve() {
        ParagraphMatcher matcher = ParagraphMatcher.orderedList("4");
        assertFalse(matcher.matches(make(a(PARAGRAPH))));

        Paragraph paragraphWithCorrectNumbering = make(a(PARAGRAPH,
            with(NUMBERING, Optional.of(NumberingLevel.ordered("4")))));
        assertTrue(matcher.matches(paragraphWithCorrectNumbering));

        Paragraph paragraphWithIncorrectLevel = make(a(PARAGRAPH,
            with(NUMBERING, Optional.of(NumberingLevel.ordered("3")))));
        assertFalse(matcher.matches(paragraphWithIncorrectLevel));

        Paragraph paragraphWithIncorrectOrdering = make(a(PARAGRAPH,
            with(NUMBERING, Optional.of(NumberingLevel.unordered("4")))));
        assertFalse(matcher.matches(paragraphWithIncorrectOrdering));
    }

    @Test
    public void matcherWithUnorderedListOnlyMatchesParagraphsWithUnrderedListAtThatLeve() {
        ParagraphMatcher matcher = ParagraphMatcher.unorderedList("4");
        assertFalse(matcher.matches(make(a(PARAGRAPH))));

        Paragraph paragraphWithCorrectNumbering = make(a(PARAGRAPH,
            with(NUMBERING, Optional.of(NumberingLevel.unordered("4")))));
        assertTrue(matcher.matches(paragraphWithCorrectNumbering));

        Paragraph paragraphWithIncorrectLevel = make(a(PARAGRAPH,
            with(NUMBERING, Optional.of(NumberingLevel.unordered("3")))));
        assertFalse(matcher.matches(paragraphWithIncorrectLevel));

        Paragraph paragraphWithIncorrectOrdering = make(a(PARAGRAPH,
            with(NUMBERING, Optional.of(NumberingLevel.ordered("4")))));
        assertFalse(matcher.matches(paragraphWithIncorrectOrdering));
    }
}
