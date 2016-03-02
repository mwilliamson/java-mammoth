package org.zwobble.mammoth.internal.xml.parsing;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlTextNode;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.zwobble.mammoth.internal.util.MammothMaps.lookup;

public class XmlParser {
    private final BiMap<String, String> namespaces;

    public XmlParser(BiMap<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    public XmlElement parseStream(InputStream inputStream) {
        NodeGenerator nodeGenerator = new NodeGenerator();
        SimpleSax.parseStream(inputStream, nodeGenerator);
        return nodeGenerator.getRoot();
    }
    
    public XmlElement parseString(String value) {
        NodeGenerator nodeGenerator = new NodeGenerator();
        SimpleSax.parseString(value, nodeGenerator);
        return nodeGenerator.getRoot();
    }
    
    private class NodeGenerator implements SimpleSaxHandler {
        private final Deque<XmlElementBuilder> elementStack;
        
        public NodeGenerator() {
            elementStack = new ArrayDeque<>();
        }
        
        public XmlElement getRoot() {
            return elementStack.getFirst().build();
        }

        @Override
        public void startElement(ElementName name, Map<ElementName, String> attributes) {
            Map<String, String> simpleAttributes = attributes
                .entrySet()
                .stream()
                .collect(toMap(
                    entry -> readName(entry.getKey()),
                    entry -> entry.getValue()));
            XmlElementBuilder element = new XmlElementBuilder(readName(name), simpleAttributes);
            elementStack.add(element);
        }

        private String readName(ElementName name) {
            if (Strings.isNullOrEmpty(name.getUri())) {
                return name.getLocalName();                
            } else if (namespaces.containsValue(name.getUri())) {
                return lookup(namespaces.inverse(), name.getUri()).get() + ":" + name.getLocalName();
            } else {
                return "{" + name.getUri() + "}" + name.getLocalName();
            }
            
        }

        @Override
        public void endElement() {
            if (elementStack.size() > 1) {
                XmlElementBuilder element = elementStack.removeLast();
                elementStack.getLast().addChild(element.build());   
            }            
        }

        @Override
        public void characters(String string) {
            elementStack.getLast().addChild(new XmlTextNode(string));
        }
    }
}
