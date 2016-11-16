package org.zwobble.mammoth.tests.styles.parsing;

import org.zwobble.mammoth.internal.html.HtmlElement;
import org.zwobble.mammoth.internal.html.HtmlNode;
import org.zwobble.mammoth.internal.html.HtmlTag;
import org.zwobble.mammoth.internal.styles.HtmlPathElement;

import java.util.List;

import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;

public class HtmlElementBuilder {
    public static HtmlElementBuilder collapsible(String tagName) {
        return new HtmlElementBuilder(tagName, true, "");
    }

    public static HtmlElementBuilder fresh(String tagName) {
        return new HtmlElementBuilder(tagName, false, "");
    }

    private final String tagName;
    private final boolean isCollapsible;
    private final String separator;

    public HtmlElementBuilder(String tagName, boolean isCollapsible, String separator) {
        this.tagName = tagName;
        this.isCollapsible = isCollapsible;
        this.separator = separator;
    }

    public HtmlElementBuilder separator(String separator) {
        return new HtmlElementBuilder(tagName, isCollapsible, separator);
    }

    public HtmlPathElement pathElement() {
        return new HtmlPathElement(tag());
    }

    public HtmlElement element(List<HtmlNode> children) {
        return new HtmlElement(tag(), children);
    }

    private HtmlTag tag() {
        return new HtmlTag(list(tagName), map(), isCollapsible, separator);
    }
}
