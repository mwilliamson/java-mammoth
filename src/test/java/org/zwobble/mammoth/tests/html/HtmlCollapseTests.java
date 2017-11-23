package org.zwobble.mammoth.tests.html;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.html.Html;
import org.zwobble.mammoth.tests.styles.parsing.HtmlElementBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;

public class HtmlCollapseTests {
    @Test
    public void collapsingDoesNothingToSingleTextNode() {
        assertThat(
            Html.collapse(list(Html.text("Bluebells"))),
            deepEquals(list(Html.text("Bluebells"))));
    }

    @Test
    public void consecutiveFreshElementsAreNotCollapsed() {
        assertThat(
            Html.collapse(list(Html.element("p"), Html.element("p"))),
            deepEquals(list(Html.element("p"), Html.element("p"))));
    }

    @Test
    public void consecutiveCollapsibleElementsAreCollapsedIfTheyHaveTheSameTagAndAttributes() {
        assertThat(
            Html.collapse(list(
                Html.collapsibleElement("p", list(Html.text("One"))),
                Html.collapsibleElement("p", list(Html.text("Two"))))),

            deepEquals(list(Html.collapsibleElement("p", list(Html.text("One"), Html.text("Two"))))));
    }

    @Test
    public void elementsWithDifferentTagNamesAreNotCollapsed() {
        assertThat(
            Html.collapse(list(
                Html.collapsibleElement("p", list(Html.text("One"))),
                Html.collapsibleElement("div", list(Html.text("Two"))))),

            deepEquals(list(
                Html.collapsibleElement("p", list(Html.text("One"))),
                Html.collapsibleElement("div", list(Html.text("Two"))))));
    }

    @Test
    public void elementsWithDifferentAttributesAreNotCollapsed() {
        // TODO: should there be some spacing when block-level elements are collapsed?
        assertThat(
            Html.collapse(list(
                Html.collapsibleElement("p", map("id", "a"), list(Html.text("One"))),
                Html.collapsibleElement("p", list(Html.text("Two"))))),

            deepEquals(list(
                Html.collapsibleElement("p", map("id", "a"), list(Html.text("One"))),
                Html.collapsibleElement("p", list(Html.text("Two"))))));
    }

    @Test
    public void childrenOfCollapsedElementCanCollapseWithChildrenOfPreviousElement() {
        assertThat(
            Html.collapse(list(
                Html.collapsibleElement("blockquote", list(
                    Html.collapsibleElement("p", list(Html.text("One"))))),
                Html.collapsibleElement("blockquote", list(
                    Html.collapsibleElement("p", list(Html.text("Two"))))))),

            deepEquals(list(
                Html.collapsibleElement("blockquote", list(
                    Html.collapsibleElement("p", list(Html.text("One"), Html.text("Two"))))))));
    }

    @Test
    public void collapsibleElementCanCollapseIntoPreviousFreshElement() {
        assertThat(
            Html.collapse(list(Html.element("p"), Html.collapsibleElement("p"))),
            deepEquals(list(Html.element("p"))));
    }

    @Test
    public void elementWithChoiceOfTagNamesCanCollapseIntoPreviousElementIfItHasOneOfThoseTagNamesAsItsMainTagName() {
        assertThat(
            Html.collapse(list(
                Html.collapsibleElement("ol"),
                Html.collapsibleElement(list("ul", "ol")))),
            deepEquals(list(Html.collapsibleElement("ol"))));

        assertThat(
            Html.collapse(list(
                Html.collapsibleElement(list("ul", "ol")),
                Html.collapsibleElement("ol"))),
            deepEquals(list(
                Html.collapsibleElement(list("ul", "ol")),
                Html.collapsibleElement("ol"))));
    }

    @Test
    public void whenSeparatorIsPresentThenSeparatorIsPrependedToCollapsedElement() {
        assertThat(
            Html.collapse(list(
                HtmlElementBuilder.fresh("pre").element(list(Html.text("Hello"))),
                HtmlElementBuilder.collapsible("pre").separator("\n").element(list(Html.text(" the"), Html.text("re")))
            )),
            deepEquals(list(
                HtmlElementBuilder.fresh("pre").element(list(
                    Html.text("Hello"),
                    Html.text("\n"),
                    Html.text(" the"),
                    Html.text("re")
                ))
            ))
        );
    }
}
