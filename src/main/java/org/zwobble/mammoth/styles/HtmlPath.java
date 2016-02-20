package org.zwobble.mammoth.styles;

import java.util.List;

public class HtmlPath {
    private final List<HtmlPathElement> elements;

    public HtmlPath(List<HtmlPathElement> elements) {
        this.elements = elements;
    }

    public List<HtmlPathElement> getElements() {
        return elements;
    }
}
