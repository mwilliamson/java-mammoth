package org.zwobble.mammoth.docx;

import org.zwobble.mammoth.documents.DocumentElement;
import org.zwobble.mammoth.documents.TextElement;
import org.zwobble.mammoth.xml.XmlElement;

public class BodyXml {
    public static DocumentElement readBodyXmlElement(XmlElement element) {
        switch (element.getName()) {
            case "w:t":
                return new TextElement(element.innerText());
            default:
                throw new UnsupportedOperationException();
        }
    }
}
