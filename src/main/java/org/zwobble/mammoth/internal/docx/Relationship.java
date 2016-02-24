package org.zwobble.mammoth.internal.docx;

public class Relationship {
    private final String target;

    public Relationship(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
