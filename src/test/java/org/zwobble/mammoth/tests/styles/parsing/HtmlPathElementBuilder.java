package org.zwobble.mammoth.tests.styles.parsing;

import org.zwobble.mammoth.internal.styles.HtmlPathElement;

import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;

public class HtmlPathElementBuilder {
    public static HtmlPathElementBuilder tagName(String tagName) {
        return new HtmlPathElementBuilder(tagName, false, "");
    }

    private final String tagName;
    private final boolean isCollapsible;
    private final String separator;

    public HtmlPathElementBuilder(String tagName, boolean isCollapsible, String separator) {
        this.tagName = tagName;
        this.isCollapsible = isCollapsible;
        this.separator = separator;
    }

    public HtmlPathElementBuilder collapsible(boolean isCollapsible) {
        return new HtmlPathElementBuilder(tagName, isCollapsible, separator);
    }

    public HtmlPathElementBuilder separator(String separator) {
        return new HtmlPathElementBuilder(tagName, isCollapsible, separator);
    }

    public HtmlPathElement build() {
        return new HtmlPathElement(list(tagName), map(), isCollapsible, separator);
    }
}
