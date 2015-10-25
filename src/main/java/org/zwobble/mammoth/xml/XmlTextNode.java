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
    public <T> T accept(XmlNodeVisitor<T> visitor) {
        return visitor.Visit(this);
    }

    @Override
    public String toString() {
        return "XmlTextNode(value=" + value + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        XmlTextNode other = (XmlTextNode) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}
