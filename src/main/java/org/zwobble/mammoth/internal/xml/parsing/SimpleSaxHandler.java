package org.zwobble.mammoth.internal.xml.parsing;

import java.util.Map;

interface SimpleSaxHandler {
    void startElement(ElementName name, Map<ElementName, String> attributes);
    void endElement();
    void characters(String string);
}
