package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.xml.*;
import org.zwobble.mammoth.internal.xml.parsing.XmlParser;

import java.io.InputStream;
import java.util.List;

import static org.zwobble.mammoth.internal.util.Lists.eagerFlatMap;
import static org.zwobble.mammoth.internal.util.Lists.list;

public class OfficeXml {
    private static NamespacePrefixes XML_NAMESPACES = NamespacePrefixes.builder()
        .put("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        .put("wp", "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing")
        .put("a", "http://schemas.openxmlformats.org/drawingml/2006/main")
        .put("pic", "http://schemas.openxmlformats.org/drawingml/2006/picture")
        .put("content-types", "http://schemas.openxmlformats.org/package/2006/content-types")
        .put("r", "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
        .put("relationships", "http://schemas.openxmlformats.org/package/2006/relationships")
        .put("v", "urn:schemas-microsoft-com:vml")
        .put("mc", "http://schemas.openxmlformats.org/markup-compatibility/2006")
        .put("office-word", "urn:schemas-microsoft-com:office:word")
        .build();

    public static XmlElement parseXml(InputStream inputStream) {
        XmlParser parser = new XmlParser(XML_NAMESPACES);
        return (XmlElement)collapseAlternateContent(parser.parseStream(inputStream)).get(0);
    }

    private static List<XmlNode> collapseAlternateContent(XmlNode node) {
        return node.accept(new XmlNodeVisitor<List<XmlNode>>() {
            @Override
            public List<XmlNode> visit(XmlElement element) {
                if (element.getName().equals("mc:AlternateContent")) {
                    return element.findChild("mc:Fallback").getChildren();
                } else {
                    XmlElement collapsedElement = new XmlElement(
                        element.getName(),
                        element.getAttributes(),
                        eagerFlatMap(element.getChildren(), OfficeXml::collapseAlternateContent));
                    return list(collapsedElement);
                }
            }

            @Override
            public List<XmlNode> visit(XmlTextNode textNode) {
                return list(textNode);
            }
        });
    }
}
