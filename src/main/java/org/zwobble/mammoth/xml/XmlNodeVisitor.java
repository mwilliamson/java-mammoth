package org.zwobble.mammoth.xml;

public interface XmlNodeVisitor<T> {
    T visit(XmlElement element);
    T Visit(XmlTextNode textNode);
}
