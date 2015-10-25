package org.zwobble.mammoth.xml;

public class ElementName {
    private final String uri;
    private final String localName;
    
    public ElementName(String uri, String localName) {
        this.uri = uri;
        this.localName = localName;
    }
    
    public String getUri() {
        return uri;
    }
    
    public String getLocalName() {
        return localName;
    }
}
