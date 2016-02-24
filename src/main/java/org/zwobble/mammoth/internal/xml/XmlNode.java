package org.zwobble.mammoth.internal.xml;

public interface XmlNode {
    String innerText();
    <T> T accept(XmlNodeVisitor<T> visitor);
}
