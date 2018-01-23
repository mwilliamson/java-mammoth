package org.zwobble.mammoth.internal.docx;

public class Relationship {
    private final String relationshipId;
    private final String target;

    public Relationship(String relationshipId, String target) {
        this.relationshipId = relationshipId;
        this.target = target;
    }

    public String getRelationshipId() {
        return relationshipId;
    }

    public String getTarget() {
        return target;
    }
}
