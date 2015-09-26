package org.zwobble.mammoth.xml;

public interface XmlNode {
    <T> T accept(XmlNodeVisitor<T> visitor);
}
