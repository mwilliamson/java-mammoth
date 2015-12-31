package org.zwobble.mammoth.docx;

import java.util.Map;
import java.util.Optional;

public class Relationships {
    private final Map<String, Relationship> relationships;

    public Relationships(Map<String, Relationship> relationships) {
        this.relationships = relationships;
    }

    public Relationship findRelationshipById(String id) {
        return Optional.ofNullable(relationships.get(id)).get();
    }
}
