package org.zwobble.mammoth.internal.xml;

import java.util.List;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.Lists.list;

public class NullXmlElement implements XmlElementLike {
    public static final XmlElementLike INSTANCE = new NullXmlElement();

    private NullXmlElement() {
    }

    @Override
    public boolean hasChild(String name) {
        return false;
    }

    @Override
    public Optional<XmlElement> findChild(String name) {
        return Optional.empty();
    }

    @Override
    public XmlElementLike findChildOrEmpty(String name) {
        return this;
    }

    @Override
    public Optional<String> getAttributeOrNone(String name) {
        return Optional.empty();
    }

    @Override
    public List<XmlNode> getChildren() {
        return list();
    }
}
