package org.zwobble.mammoth.internal.docx;

public class Relationship {
    private final String relationshipId;
    private final String target;
    private final String type;

    public Relationship(String relationshipId, String target, String type) {
        this.relationshipId = relationshipId;
        this.target = target;
        this.type = type;
    }

    public String getRelationshipId() {
        return relationshipId;
    }

    public String getTarget() {
        return target;
    }

    public String getType() {
        return type;
    }
}
