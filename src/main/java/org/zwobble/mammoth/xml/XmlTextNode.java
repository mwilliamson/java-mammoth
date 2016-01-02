package org.zwobble.mammoth.xml;

public class XmlTextNode implements XmlNode {
    private final String value;

    public XmlTextNode(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }

    @Override
    public String innerText() {
        return value;
    }

    @Override
    public <T> T accept(XmlNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "XmlTextNode(value=" + value + ")";
    }
}
