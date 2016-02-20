package org.zwobble.mammoth.styles;

import static org.zwobble.mammoth.util.MammothLists.list;

public class StyleMap {
    public static final StyleMap EMPTY = new StyleMap(new HtmlPath(list()));

    private final HtmlPath underline;

    public StyleMap(HtmlPath underline) {
        this.underline = underline;
    }

    public HtmlPath getUnderline() {
        return underline;
    }
}
