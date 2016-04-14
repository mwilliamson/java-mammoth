package org.zwobble.mammoth.internal.xml.parsing;

import org.zwobble.mammoth.internal.xml.NamespacePrefixes;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlTextNode;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
        private final List<XmlElementBuilder> elementStack;
        
        public NodeGenerator() {
            elementStack = new ArrayList<>();
        }
        
        public XmlElement getRoot() {
            return elementStack.get(0).build();
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
                return namespaces.prefixForUri(name.getUri())
                    .map(prefix -> prefix + ":" + name.getLocalName())
                    .orElseGet(() -> "{" + name.getUri() + "}" + name.getLocalName());
            }
            
        }

        @Override
        public void endElement() {
            if (elementStack.size() > 1) {
                XmlElementBuilder element = elementStack.remove(elementStack.size() - 1);
                elementStack.get(elementStack.size() - 1).addChild(element.build());
            }            
        }

        @Override
        public void characters(String string) {
            elementStack.get(elementStack.size() - 1).addChild(new XmlTextNode(string));
        }
    }
}
