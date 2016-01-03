package org.zwobble.mammoth.xml;

import java.util.List;
import java.util.Map;

import static org.zwobble.mammoth.util.MammothLists.list;
import static org.zwobble.mammoth.util.MammothMaps.map;

public class XmlNodes {
    public static XmlElement element(String name) {
        return element(name, list());
    }

    public static XmlElement element(String name, Map<String, String> attributes) {
        return element(name, attributes, list());
    }

    public static XmlElement element(String name, List<XmlNode> children) {
        return element(name, map(), children);
    }

    public static XmlElement element(String name, Map<String, String> attributes, List<XmlNode> children) {
        return new XmlElement(name, attributes, children);
    }

    public static XmlTextNode text(String value) {
        return new XmlTextNode(value);
    }
}
