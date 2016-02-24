package org.zwobble.mammoth.internal.styles;

public class StyleMapping<T> {
    private final DocumentElementMatcher<T> matcher;
    private final HtmlPath htmlPath;

    public StyleMapping(DocumentElementMatcher<T> matcher, HtmlPath htmlPath) {
        this.matcher = matcher;
        this.htmlPath = htmlPath;
    }

    public boolean matches(T element) {
        return matcher.matches(element);
    }

    public HtmlPath getHtmlPath() {
        return htmlPath;
    }
}
