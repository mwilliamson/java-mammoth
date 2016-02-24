package org.zwobble.mammoth.internal.xml.parsing;

import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class XmlElementBuilder {
    private final String name;
    private final Map<String, String> attributes;
    private final List<XmlNode> children;

    XmlElementBuilder(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes;
        this.children = new ArrayList<>();
    }
    
    XmlElement build() {
        return new XmlElement(name, attributes, children);
    }

    void addChild(XmlNode node) {
        children.add(node);
    }
}
