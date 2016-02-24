package org.zwobble.mammoth.internal.xml;

public interface XmlNodeVisitor<T> {
    T visit(XmlElement element);
    T visit(XmlTextNode textNode);
}
