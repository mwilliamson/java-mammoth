package org.zwobble.mammoth.tests.styles.parsing;

import org.zwobble.mammoth.internal.html.HtmlTag;
import org.zwobble.mammoth.internal.styles.HtmlPathElement;

import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;

public class HtmlPathElementBuilder {
    public static HtmlPathElementBuilder collapsible(String tagName) {
        return new HtmlPathElementBuilder(tagName, true, "");
    }

    private final String tagName;
    private final boolean isCollapsible;
    private final String separator;

    public HtmlPathElementBuilder(String tagName, boolean isCollapsible, String separator) {
        this.tagName = tagName;
        this.isCollapsible = isCollapsible;
        this.separator = separator;
    }

    public HtmlPathElementBuilder separator(String separator) {
        return new HtmlPathElementBuilder(tagName, isCollapsible, separator);
    }

    public HtmlPathElement build() {
        return new HtmlPathElement(new HtmlTag(list(tagName), map(), isCollapsible, separator));
    }
}
