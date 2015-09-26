package org.zwobble.mammoth.xml;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class XmlElement implements XmlNode {
    public XmlElement(String name) {
        this(name, ImmutableMap.of());
    }
    
    public XmlElement(String name, Map<String, String> attributes) {
        this(name, attributes, ImmutableList.of());
    }
    
    @Getter
    private final String name;
    private final Map<String, String> attributes;
    private final List<XmlNode> children;
}
