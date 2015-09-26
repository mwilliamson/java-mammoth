package org.zwobble.mammoth.tests.xml;

import java.io.ByteArrayInputStream;

import org.junit.Test;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlParser;
import org.zwobble.mammoth.xml.XmlTextNode;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import lombok.val;

public class XmlParserTests {
    private final XmlParser parser = new XmlParser(ImmutableBiMap.of());
    
    @Test
    public void canParseSelfClosingElement() {
        assertThat(
            parser.parseString("<body/>"),
            is(new XmlElement("body")));
        assertThat(
            parser.parseString("<values/>"),
            is(new XmlElement("values")));
    }
    
    @Test
    public void canParseEmptyElementWithSeparateClosingTag() {
        assertThat(
            parser.parseString("<body></body>"),
            is(new XmlElement("body")));
    }
    
    @Test
    public void canParseAttributesOfTag() {
        assertThat(
            parser.parseString("<body name='bob'></body>"),
            is(new XmlElement("body", ImmutableMap.of("name", "bob"))));
    }
    
    @Test
    public void canParseElementWithDescendantElements() {
        assertThat(
            parser.parseString("<body><a><b/></a><a/></body>"),
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
            parser.parseString("<body>Hello!</body>"),
            is(new XmlElement("body", ImmutableMap.of(), ImmutableList.of(
                new XmlTextNode("Hello!")
            ))));
    }
    
    @Test
    public void unmappedNamespaceUrisInElementNamesAreIncludedInBracesAsPrefix() {
        assertThat(
            parser.parseString("<w:body xmlns:w='word'/>"),
            is(new XmlElement("{word}body")));
    }
    
    @Test
    public void unmappedNamespaceUrisInAttributeNamesAreIncludedInBracesAsPrefix() {
        assertThat(
            parser.parseString("<body xmlns:w='word' w:name='bob'></body>"),
            is(new XmlElement("body", ImmutableMap.of("{word}name", "bob"))));
    }
    
    @Test
    public void mappedNamespaceUrisInElementNamesArePrefixedToLocalNameWithColon() {
        val parser = new XmlParser(ImmutableBiMap.of("x", "word")); 
        assertThat(
            parser.parseString("<w:body xmlns:w='word'/>"),
            is(new XmlElement("x:body")));
    }
    
    @Test
    public void mappedNamespaceUrisInAttributeNamesArePrefixedToLocalNameWithColon() {
        val parser = new XmlParser(ImmutableBiMap.of("x", "word")); 
        assertThat(
            parser.parseString("<body xmlns:w='word' w:name='bob'/>"),
            is(new XmlElement("body", ImmutableMap.of("x:name", "bob"))));
    }
    
    @Test
    public void canParseInputStream() {
        assertThat(
            parser.parseStream(new ByteArrayInputStream("<body/>".getBytes())),
            is(new XmlElement("body")));
    }
}
