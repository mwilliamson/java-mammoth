package org.zwobble.mammoth.xml;

public interface XmlNode {
    String innerText();
    <T> T accept(XmlNodeVisitor<T> visitor);
}
