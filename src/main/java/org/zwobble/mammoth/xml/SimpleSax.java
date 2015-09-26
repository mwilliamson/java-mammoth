package org.zwobble.mammoth.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.stream.IntStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import static java.util.stream.Collectors.toMap;

import lombok.val;

public class SimpleSax {
    public static void parseStream(InputStream input, SimpleSaxHandler handler) {
        parseInputSource(new InputSource(input), handler);
    }
    
    public static void parseString(String value, SimpleSaxHandler handler) {
        parseInputSource(new InputSource(new StringReader(value)), handler);
    }

    private static void parseInputSource(InputSource inputSource, SimpleSaxHandler handler) {
        val parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        try {
            val saxParser = parserFactory.newSAXParser();
            val xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    final ElementName name = new ElementName(uri, localName);
                    val attributesMap = IntStream.range(0, attributes.getLength())
                        .boxed()
                        .collect(toMap(
                            index -> new ElementName(attributes.getURI(index), attributes.getLocalName(index)),
                            attributes::getValue
                        ));
                    handler.startElement(name, attributesMap);
                }
                
                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    handler.endElement();
                }
                
                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    handler.characters(new String(ch, start, length));
                }
            });
            xmlReader.parse(inputSource);
        } catch (ParserConfigurationException | SAXException | IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
