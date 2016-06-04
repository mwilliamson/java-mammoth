package org.zwobble.mammoth.internal.xml;

import java.util.List;
import java.util.Optional;

public interface XmlElementLike {
    boolean hasChild(String name);
    XmlElementLike findChildOrEmpty(String name);
    Optional<String> getAttributeOrNone(String name);
    List<XmlNode> getChildren();
}
