package org.zwobble.mammoth.internal.xml;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class XmlWriter {
    public static String toString(XmlElement element, NamespacePrefixes namespaces) {
      return toString(element, namespaces, false);
    }
    
    public static String toString(XmlElement element, NamespacePrefixes namespaces, boolean shouldCreateDocumentFragment) {
      try {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          XmlWriter writer = new XmlWriter(createXmlWriter(outputStream), namespaces);
          if(shouldCreateDocumentFragment) {
          	writer.writeDocumentFragment(element);
          } else {
          	writer.writeDocument(element);
          }
          
          return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
      } catch (XMLStreamException exception) {
          throw new RuntimeException(exception);
      }
  }

    private static XMLStreamWriter createXmlWriter(ByteArrayOutputStream outputStream) throws XMLStreamException {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        return outputFactory.createXMLStreamWriter(outputStream, "UTF-8");
    }

    private final XMLStreamWriter writer;
    private final NamespacePrefixes namespaces;

    private XmlWriter(XMLStreamWriter writer, NamespacePrefixes namespaces) {
        this.writer = writer;
        this.namespaces = namespaces;
    }

    private void writeDocumentFragment(XmlElement element) throws XMLStreamException {
      writeStartElement(element);
      writeNamespaces(namespaces);
      writeAttributes(element);
      writeNodes(element.getChildren());
      writer.writeEndElement();
  }
    
    private void writeDocument(XmlElement element) throws XMLStreamException {
        writer.writeStartDocument("UTF-8", "1.0");
        writeDocumentFragment(element);
        writer.writeEndDocument();
    }

    private void writeNodes(List<XmlNode> children) {
        for (XmlNode child : children) {
            writeNode(child);
        }
    }

    private void writeNode(XmlNode child) {
        child.accept(new XmlNodeVisitor<Object>() {
            @Override
            public Object visit(XmlElement element) {
                try {
                    writeStartElement(element);
                    writeAttributes(element);
                    writeNodes(element.getChildren());
                    writer.writeEndElement();
                } catch (XMLStreamException exception) {
                    throw new RuntimeException(exception);
                }
                return null;
            }

            @Override
            public Object visit(XmlTextNode textNode) {
                try {
                    writer.writeCharacters(textNode.getValue());
                } catch (XMLStreamException exception) {
                    throw new RuntimeException(exception);
                }
                return null;
            }
        });
    }

    private void writeNamespaces(NamespacePrefixes namespaces) throws XMLStreamException {
        for (NamespacePrefix namespace : namespaces) {
            Optional<String> prefix = namespace.getPrefix();
            if (prefix.isPresent()) {
                writer.writeNamespace(prefix.get(), namespace.getUri());
            } else {
                writer.writeDefaultNamespace(namespace.getUri());
            }
        }
    }

    private void writeStartElement(XmlElement element) throws XMLStreamException {
        XmlName name = readName(element.getName());
        Optional<String> prefix = name.namespace.getPrefix();
        if (prefix.isPresent()) {
            writer.writeStartElement(prefix.get(), name.localName, name.namespace.getUri());
        } else {
            writer.writeStartElement(name.localName);
        }
    }

    private void writeAttributes(XmlElement element) throws XMLStreamException {
        for (Map.Entry<String, String> attribute : element.getAttributes().entrySet()) {
            writeAttribute(attribute);
        }
    }

    private void writeAttribute(Map.Entry<String, String> attribute) throws XMLStreamException {
        XmlName name = readName(attribute.getKey());
        Optional<String> prefix = name.namespace.getPrefix();
        if (prefix.isPresent()) {
            writer.writeAttribute(prefix.get(), name.namespace.getUri(), name.localName, attribute.getValue());
        } else {
            writer.writeAttribute(name.localName, attribute.getValue());
        }
    }

    private XmlName readName(String name) {
        String[] parts = name.split(":", 2);
        if (parts.length == 1) {
            return new XmlName(namespaces.defaultNamespace().get(), parts[0]);
        } else {
            String prefix = parts[0];
            String localName = parts[1];
            NamespacePrefix namespace = namespaces.lookupPrefix(prefix)
                .orElseThrow(() -> new RuntimeException("Could not find namespace for prefix: " + prefix));
            return new XmlName(namespace, localName);
        }
    }

    private static class XmlName {
        private final NamespacePrefix namespace;
        private final String localName;

        private XmlName(NamespacePrefix namespace, String localName) {
            this.namespace = namespace;
            this.localName = localName;
        }
    }
}
