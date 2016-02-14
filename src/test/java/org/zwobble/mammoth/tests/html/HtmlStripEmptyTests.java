package org.zwobble.mammoth.tests.html;

import org.junit.Test;
import org.zwobble.mammoth.html.Html;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.util.MammothLists.list;

public class HtmlStripEmptyTests {
    @Test
    public void textNodesWithTextAreNotStripped() {
        assertThat(
            Html.stripEmpty(list(Html.text("H"))),
            deepEquals(list(Html.text("H"))));
    }

    @Test
    public void emptyTextNodesAreStripped() {
        assertThat(
            Html.stripEmpty(list(Html.text(""))),
            deepEquals(list()));
    }

    @Test
    public void elementsWithNonEmptyChildrenAreNotStripped() {
        assertThat(
            Html.stripEmpty(list(Html.element("p", list(Html.text("H"))))),
            deepEquals(list(Html.element("p", list(Html.text("H"))))));
    }

    @Test
    public void elementsWithNoChildrenAreStripped() {
        assertThat(
            Html.stripEmpty(list(Html.element("p"))),
            deepEquals(list()));
    }

    @Test
    public void elementsWithOnlyEmptyChildrenAreStripped() {
        assertThat(
            Html.stripEmpty(list(Html.element("p", list(Html.text(""))))),
            deepEquals(list()));
    }

    @Test
    public void emptyChildrenAreRemoved() {
        assertThat(
            Html.stripEmpty(list(
                Html.element("ul", list(
                    Html.element("li", list(Html.text(""))),
                    Html.element("li", list(Html.text("H"))))))),

            deepEquals(list(Html.element("ul", list(
                Html.element("li", list(Html.text("H"))))))));
    }
}
