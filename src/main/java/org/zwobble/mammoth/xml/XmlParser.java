package org.zwobble.mammoth.xml;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

import lombok.val;

public class XmlParser {
    public static XmlElement parseString(String value) {
        val nodeGenerator = new NodeGenerator();
        SimpleSax.parseString(value, nodeGenerator);
        return nodeGenerator.getRoot();
    }
    
    private static class NodeGenerator implements SimpleSaxHandler {
        private final Deque<XmlElementBuilder> elementStack;
        
        public NodeGenerator() {
            elementStack = new ArrayDeque<>();
        }
        
        public XmlElement getRoot() {
            return elementStack.getFirst().build();
        }

        @Override
        public void startElement(ElementName name, Map<ElementName, String> attributes) {
            val simpleAttributes = attributes
                .entrySet()
                .stream()
                .collect(toMap(
                    entry -> entry.getKey().getLocalName(),
                    entry -> entry.getValue()));
            val element = new XmlElementBuilder(name.getLocalName(), simpleAttributes);
            elementStack.add(element);
        }

        @Override
        public void endElement() {
            if (elementStack.size() > 1) {
                val element = elementStack.removeLast();
                elementStack.getLast().addChild(element.build());   
            }            
        }

        @Override
        public void characters(String string) {
            elementStack.getLast().addChild(new XmlTextNode(string));
        }
    }
}
