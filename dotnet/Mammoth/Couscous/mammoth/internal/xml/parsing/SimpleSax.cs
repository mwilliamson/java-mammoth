using Mammoth.Couscous.java.io;
using Mammoth.Couscous.java.util;
using System.Xml;

namespace Mammoth.Couscous.org.zwobble.mammoth.@internal.xml.parsing {
    internal static class SimpleSax {
        internal static void parseStream(InputStream input, SimpleSaxHandler handler) {
            var reader = XmlReader.Create(input.Stream);
            while (reader.Read()) {
                switch (reader.NodeType) {
                    case XmlNodeType.Element:
                        var name = new ElementName(reader.NamespaceURI, reader.LocalName);
                        var attributes = new HashMap<ElementName, string>();
                        var isEmpty = reader.IsEmptyElement;
                        for (int attributeIndex = 0; attributeIndex < reader.AttributeCount; attributeIndex++) {
                            reader.MoveToAttribute(attributeIndex);
                            attributes.put(new ElementName(reader.NamespaceURI, reader.LocalName), reader.Value);
                        }
                        handler.startElement(name, attributes);
                        if (isEmpty) {
                            handler.endElement();
                        }
                        break;
                    case XmlNodeType.CDATA:
                    case XmlNodeType.Text:
                    case XmlNodeType.SignificantWhitespace:
                        handler.characters(reader.Value);
                        break;
                    case XmlNodeType.EntityReference:
                       throw new System.NotImplementedException();
                    case XmlNodeType.EndElement:
                        handler.endElement();
                        break;
               } 
            }
        }
        
        internal static void parseString(string value, SimpleSaxHandler handler) {
            throw new System.NotImplementedException();
        }
    }
}
