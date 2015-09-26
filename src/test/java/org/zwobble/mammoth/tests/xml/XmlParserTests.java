package org.zwobble.mammoth.tests.xml;

import org.junit.Test;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlParser;
import org.zwobble.mammoth.xml.XmlTextNode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class XmlParserTests {
    @Test
    public void canParseSelfClosingElement() {
        assertThat(
            XmlParser.parseString("<body/>"),
            is(new XmlElement("body")));
        assertThat(
            XmlParser.parseString("<values/>"),
            is(new XmlElement("values")));
    }
    
    @Test
    public void canParseEmptyElementWithSeparateClosingTag() {
        assertThat(
            XmlParser.parseString("<body></body>"),
            is(new XmlElement("body")));
    }
    
    @Test
    public void canParseAttributesOfTag() {
        assertThat(
            XmlParser.parseString("<body name='bob'></body>"),
            is(new XmlElement("body", ImmutableMap.of("name", "bob"))));
    }
    
    @Test
    public void canParseElementWithDescendantElements() {
        assertThat(
            XmlParser.parseString("<body><a><b/></a><a/></body>"),
            is(new XmlElement("body", ImmutableMap.of(), ImmutableList.of(
                new XmlElement("a", ImmutableMap.of(), ImmutableList.of(
                    new XmlElement("b")
                )),
                new XmlElement("a")
            ))));
    }
    
    @Test
    public void canParseTextNodes() {
        assertThat(
            XmlParser.parseString("<body>Hello!</body>"),
            is(new XmlElement("body", ImmutableMap.of(), ImmutableList.of(
                new XmlTextNode("Hello!")
            ))));
    }
    
    @Test
    public void unmappedNamespaceUrisInElementNamesAreIncludedInBracesAsPrefix() {
        assertThat(
            XmlParser.parseString("<w:body xmlns:w='word'/>"),
            is(new XmlElement("{word}body")));
    }
    
    @Test
    public void unmappedNamespaceUrisInAttributeNamesAreIncludedInBracesAsPrefix() {
        assertThat(
            XmlParser.parseString("<body xmlns:w='word' w:name='bob'></body>"),
            is(new XmlElement("body", ImmutableMap.of("{word}name", "bob"))));
    }
}
