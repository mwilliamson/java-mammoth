package org.zwobble.mammoth.styles;

import org.zwobble.mammoth.documents.Paragraph;

public class StyleMapping {
    private final ParagraphMatcher matcher;
    private final HtmlPath htmlPath;

    public StyleMapping(ParagraphMatcher matcher, HtmlPath htmlPath) {
        this.matcher = matcher;
        this.htmlPath = htmlPath;
    }

    public boolean matches(Paragraph paragraph) {
        return matcher.matches(paragraph);
    }

    public HtmlPath getHtmlPath() {
        return htmlPath;
    }
}
