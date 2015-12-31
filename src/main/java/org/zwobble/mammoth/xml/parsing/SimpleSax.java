package org.zwobble.mammoth.xml.parsing;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.stream.IntStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import static java.util.stream.Collectors.toMap;

class SimpleSax {
    static void parseStream(InputStream input, SimpleSaxHandler handler) {
        parseInputSource(new InputSource(input), handler);
    }
    
    static void parseString(String value, SimpleSaxHandler handler) {
        parseInputSource(new InputSource(new StringReader(value)), handler);
    }

    private static void parseInputSource(InputSource inputSource, SimpleSaxHandler handler) {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        try {
            SAXParser saxParser = parserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    ElementName name = new ElementName(uri, localName);
                    Map<ElementName, String> attributesMap = IntStream.range(0, attributes.getLength())
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
