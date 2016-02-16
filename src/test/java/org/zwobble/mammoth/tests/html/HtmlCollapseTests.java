package org.zwobble.mammoth.tests.html;

import org.junit.Test;
import org.zwobble.mammoth.html.Html;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.util.MammothLists.list;

public class HtmlCollapseTests {
    @Test
    public void collapsingDoesNothingToSingleTextNode() {
        assertThat(
            Html.collapse(list(Html.text("Bluebells"))),
            deepEquals(list(Html.text("Bluebells"))));
    }
}
