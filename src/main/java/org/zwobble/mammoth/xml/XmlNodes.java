package org.zwobble.mammoth.xml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

public class XmlNodes {
    public static XmlElement element(String name) {
        return element(name, ImmutableList.of());
    }

    public static XmlElement element(String name, Map<String, String> attributes) {
        return element(name, attributes, ImmutableList.of());
    }

    public static XmlElement element(String name, List<XmlNode> children) {
        return element(name, ImmutableMap.of(), children);
    }

    public static XmlElement element(String name, Map<String, String> attributes, List<XmlNode> children) {
        return new XmlElement(name, attributes, children);
    }
}
