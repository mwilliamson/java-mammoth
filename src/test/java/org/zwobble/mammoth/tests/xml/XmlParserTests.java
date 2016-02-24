package org.zwobble.mammoth.tests.xml;

import com.google.common.collect.ImmutableBiMap;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsEmptyIterable;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlNode;
import org.zwobble.mammoth.internal.xml.XmlTextNode;
import org.zwobble.mammoth.internal.xml.parsing.XmlParser;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.zwobble.mammoth.internal.util.MammothLists.list;
import static org.zwobble.mammoth.internal.util.MammothMaps.map;

public class XmlParserTests {
    private final XmlParser parser = new XmlParser(ImmutableBiMap.of());
    
    @Test
    public void canParseSelfClosingElement() {
        assertThat(
            parser.parseString("<body/>"),
            isElement("body"));
        assertThat(
            parser.parseString("<values/>"),
            isElement("values"));
    }
    
    @Test
    public void canParseEmptyElementWithSeparateClosingTag() {
        assertThat(
            parser.parseString("<body></body>"),
            isElement("body"));
    }
    
    @Test
    public void canParseAttributesOfTag() {
        assertThat(
            parser.parseString("<body name='bob'></body>"),
            isElement("body", map("name", "bob")));
    }
    
    @Test
    public void canParseElementWithDescendantElements() {
        assertThat(
            parser.parseString("<body><a><b/></a><a/></body>"),
            isElement("body", map(), list(
                isElement("a", map(), list(
                    isElement("b"))),
                isElement("a"))));
    }

    @Test
    public void canParseTextNodes() {
        assertThat(
            parser.parseString("<body>Hello!</body>"),
            isElement("body", map(), list(
                isTextNode("Hello!"))));
    }
    
    @Test
    public void unmappedNamespaceUrisInElementNamesAreIncludedInBracesAsPrefix() {
        assertThat(
            parser.parseString("<w:body xmlns:w='word'/>"),
            isElement("{word}body"));
    }
    
    @Test
    public void unmappedNamespaceUrisInAttributeNamesAreIncludedInBracesAsPrefix() {
        assertThat(
            parser.parseString("<body xmlns:w='word' w:name='bob'></body>"),
            isElement("body", map("{word}name", "bob")));
    }
    
    @Test
    public void mappedNamespaceUrisInElementNamesArePrefixedToLocalNameWithColon() {
        XmlParser parser = new XmlParser(ImmutableBiMap.of("x", "word")); 
        assertThat(
            parser.parseString("<w:body xmlns:w='word'/>"),
            isElement("x:body"));
    }
    
    @Test
    public void mappedNamespaceUrisInAttributeNamesArePrefixedToLocalNameWithColon() {
        XmlParser parser = new XmlParser(ImmutableBiMap.of("x", "word")); 
        assertThat(
            parser.parseString("<body xmlns:w='word' w:name='bob'/>"),
            isElement("body", map("x:name", "bob")));
    }
    
    @Test
    public void canParseInputStream() {
        assertThat(
            parser.parseStream(new ByteArrayInputStream("<body/>".getBytes())),
            isElement("body"));
    }

    private Matcher<XmlElement> isElement(String name) {
        return isElement(name, map());
    }

    private Matcher<XmlElement> isElement(String name, Map<String, String> attributes) {
        return isElement(name, attributes, list());
    }

    private Matcher<XmlElement> isElement(String name, Map<String, String> attributes, List<Matcher<? extends XmlNode>> children) {
        return allOf(
            hasProperty("name", equalTo(name)),
            hasProperty("attributes", equalTo(attributes)),
            hasProperty("children", isNodes(children)));
    }

    private Matcher<Iterable<XmlNode>> isNodes(List<Matcher<? extends XmlNode>> children) {
        if (children.isEmpty()) {
            return (Matcher)new IsEmptyIterable<>();
        } else {
            return new IsIterableContainingInOrder<>((List) children);
        }
    }

    private Matcher<XmlNode> isTextNode(String value) {
        return allOf(
            instanceOf(XmlTextNode.class),
            hasProperty("value", equalTo(value)));
    }
}
