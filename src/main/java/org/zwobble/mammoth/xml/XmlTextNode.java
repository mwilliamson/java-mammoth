package org.zwobble.mammoth.xml;

import lombok.Value;

@Value
public class XmlTextNode implements XmlNode {
    private String value;

    @Override
    public <T> T accept(XmlNodeVisitor<T> visitor) {
        return visitor.Visit(this);
    }
}
