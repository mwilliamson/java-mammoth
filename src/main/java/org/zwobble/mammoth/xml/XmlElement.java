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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result
                 + ((children == null) ? 0 : children.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        XmlElement other = (XmlElement) obj;
        if (attributes == null) {
            if (other.attributes != null)
                return false;
        } else if (!attributes.equals(other.attributes))
            return false;
        if (children == null) {
            if (other.children != null)
                return false;
        } else if (!children.equals(other.children))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
