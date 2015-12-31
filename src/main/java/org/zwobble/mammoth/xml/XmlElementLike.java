package org.zwobble.mammoth.xml;

import java.util.Optional;

public interface XmlElementLike {
    XmlElementLike findChildOrEmpty(String name);
    Optional<String> getAttributeOrNone(String name);
}
