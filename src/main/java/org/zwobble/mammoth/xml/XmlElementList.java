package org.zwobble.mammoth.xml;

import java.util.Iterator;
import java.util.List;

public class XmlElementList implements Iterable<XmlElement> {
    private final List<XmlElement> elements;

    public XmlElementList(List<XmlElement> elements) {
        this.elements = elements;
    }

    @Override
    public Iterator<XmlElement> iterator() {
        return elements.iterator();
    }
}
