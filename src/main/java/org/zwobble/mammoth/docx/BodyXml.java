package org.zwobble.mammoth.docx;

import com.google.common.collect.ImmutableList;
import org.zwobble.mammoth.documents.DocumentElement;
import org.zwobble.mammoth.documents.ParagraphElement;
import org.zwobble.mammoth.documents.RunElement;
import org.zwobble.mammoth.documents.TextElement;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlNode;

import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

public class BodyXml {
    public static DocumentElement readBodyXmlElement(XmlElement element) {
        switch (element.getName()) {
            case "w:t":
                return new TextElement(element.innerText());
            case "w:r":
                return new RunElement(readElements(element.children()));
            case "w:p":
                return new ParagraphElement(readElements(element.children()));
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static List<DocumentElement> readElements(Iterable<XmlNode> nodes) {
        return ImmutableList.copyOf(
            transform(
                filter(nodes, XmlElement.class),
                BodyXml::readBodyXmlElement));
    }
}
