package org.zwobble.mammoth.xml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

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

    public XmlElementList findChildren(String name) {
        Iterable<XmlElement> children = Iterables.concat(
            Iterables.transform(
                elements,
                element -> element.findChildren(name)));
        return new XmlElementList(ImmutableList.copyOf(children));
    }
}
