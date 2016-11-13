package org.zwobble.mammoth.internal.xml.parsing;

import org.zwobble.mammoth.internal.xml.NamespacePrefixes;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlTextNode;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import static org.zwobble.mammoth.internal.util.Maps.eagerMapKeys;
import static org.zwobble.mammoth.internal.util.Strings.isNullOrEmpty;

public class XmlParser {
    private final NamespacePrefixes namespaces;

    public XmlParser(NamespacePrefixes namespaces) {
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
            Map<String, String> simpleAttributes = eagerMapKeys(attributes, this::readName);
            XmlElementBuilder element = new XmlElementBuilder(readName(name), simpleAttributes);
            elementStack.add(element);
        }

        private String readName(ElementName name) {
            if (isNullOrEmpty(name.getUri())) {
                return name.getLocalName();                
            } else {
                return namespaces.lookupUri(name.getUri())
                    .map(namespace -> namespace.getPrefix().map(prefix -> prefix + ":").orElse("") + name.getLocalName())
                    .orElseGet(() -> "{" + name.getUri() + "}" + name.getLocalName());
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
