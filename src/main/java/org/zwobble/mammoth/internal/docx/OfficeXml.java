package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.xml.*;
import org.zwobble.mammoth.internal.xml.parsing.XmlParser;

import java.io.InputStream;
import java.util.List;

import static org.zwobble.mammoth.internal.util.Lists.eagerFlatMap;
import static org.zwobble.mammoth.internal.util.Lists.list;

public class OfficeXml {
    private static final NamespacePrefixes XML_NAMESPACES = NamespacePrefixes.builder()
        // Transitional format
        .put("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        .put("r", "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
        .put("wp", "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing")
        .put("a", "http://schemas.openxmlformats.org/drawingml/2006/main")
        .put("pic", "http://schemas.openxmlformats.org/drawingml/2006/picture")

        // Strict format
        .put("w", "http://purl.oclc.org/ooxml/wordprocessingml/main")
        .put("r", "http://purl.oclc.org/ooxml/officeDocument/relationships")
        .put("wp", "http://purl.oclc.org/ooxml/drawingml/wordprocessingDrawing")
        .put("a", "http://purl.oclc.org/ooxml/drawingml/main")
        .put("pic", "http://purl.oclc.org/ooxml/drawingml/picture")

        // Common
        .put("content-types", "http://schemas.openxmlformats.org/package/2006/content-types")
        .put("relationships", "http://schemas.openxmlformats.org/package/2006/relationships")
        .put("mc", "http://schemas.openxmlformats.org/markup-compatibility/2006")
        .put("v", "urn:schemas-microsoft-com:vml")
        .put("office-word", "urn:schemas-microsoft-com:office:word")

        // [MS-DOCX]: Word Extensions to the Office Open XML (.docx) File Format
        // https://learn.microsoft.com/en-us/openspecs/office_standards/ms-docx/b839fe1f-e1ca-4fa6-8c26-5954d0abbccd
        .put("wordml", "http://schemas.microsoft.com/office/word/2010/wordml")

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
                    return element.findChildOrEmpty("mc:Fallback").getChildren();
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
