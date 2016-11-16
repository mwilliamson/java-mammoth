package org.zwobble.mammoth.internal.html;

import java.util.List;
import java.util.Map;

public class HtmlTag {
    private final List<String> tagNames;
    private final Map<String, String> attributes;
    private final boolean isCollapsible;
    private final String separator;

    public HtmlTag(List<String> tagNames, Map<String, String> attributes, boolean isCollapsible, String separator) {
        this.tagNames = tagNames;
        this.attributes = attributes;
        this.isCollapsible = isCollapsible;
        this.separator = separator;
    }

    public List<String> getTagNames() {
        return tagNames;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public boolean isCollapsible() {
        return isCollapsible;
    }

    public String getSeparator() {
        return separator;
    }
}
