package org.zwobble.mammoth.xml;

import java.util.Map;

public interface SimpleSaxHandler {
    void startElement(ElementName name, Map<ElementName, String> attributes);
    void endElement();
    void characters(String string);
}
