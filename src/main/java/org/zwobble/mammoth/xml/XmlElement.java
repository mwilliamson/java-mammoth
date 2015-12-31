package org.zwobble.mammoth.xml;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class XmlElement implements XmlNode {
    public XmlElement(String name) {
        this(name, ImmutableMap.of());
    }
    
    public XmlElement(String name, Map<String, String> attributes) {
        this(name, attributes, ImmutableList.of());
    }
    
    private final String name;
    private final Map<String, String> attributes;
    private final List<XmlNode> children;
    
    public XmlElement(String name, Map<String, String> attributes, List<XmlNode> children) {
        this.name = name;
        this.attributes = attributes;
        this.children = children;
    }
    
    public String getName() {
        return name;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public List<XmlNode> getChildren() {
        return children;
    }

    @Override
    public <T> T accept(XmlNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Iterable<XmlNode> children() {
        return children;
    }

    @Override
    public String toString() {
        return "XmlElement(name=" + name + ", attributes=" + attributes
               + ", children=" + children + ")";
    }
}
