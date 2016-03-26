package org.zwobble.mammoth.internal.xml;

import java.util.Iterator;
import java.util.List;

import static org.zwobble.mammoth.internal.util.MammothLists.eagerFlatMap;

public class XmlElementList implements Iterable<XmlElement> {
    private final List<XmlElement> elements;

    public XmlElementList(List<XmlElement> elements) {
        this.elements = elements;
    }

    @Override
    public Iterator<XmlElement> iterator() {
        return elements.iterator();
    }

    public XmlElementList findChildren(String name) {
        return new XmlElementList(eagerFlatMap(
            elements,
            element -> element.findChildren(name)));
    }
}
