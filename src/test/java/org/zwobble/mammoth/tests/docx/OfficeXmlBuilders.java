package org.zwobble.mammoth.tests.docx;

import org.zwobble.mammoth.internal.xml.XmlElement;

import static java.util.Arrays.asList;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.internal.xml.XmlNodes.element;

public class OfficeXmlBuilders {
    public static XmlElement wTr(XmlElement... children) {
        return element("w:tr", asList(children));
    }

    public static XmlElement wTc(XmlElement... children) {
        return element("w:tc", asList(children));
    }

    public static XmlElement wTcPr(XmlElement... children) {
        return element("w:tcPr", asList(children));
    }

    public static XmlElement wGridspan(String val) {
        return element("w:gridSpan", map("w:val", val));
    }

    public static XmlElement wVmerge(String val) {
        return element("w:vMerge", map("w:val", val));
    }
}
