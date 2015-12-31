package org.zwobble.mammoth.xml.parsing;

class ElementName {
    private final String uri;
    private final String localName;
    
    ElementName(String uri, String localName) {
        this.uri = uri;
        this.localName = localName;
    }
    
    String getUri() {
        return uri;
    }
    
    String getLocalName() {
        return localName;
    }
}
