package org.zwobble.mammoth.documents;

public class TextElement implements DocumentElement {
    private final String value;

    public TextElement(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
