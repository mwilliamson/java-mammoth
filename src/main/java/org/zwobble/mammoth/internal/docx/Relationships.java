package org.zwobble.mammoth.internal.docx;

import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.MammothMaps.map;

public class Relationships {
    public static final Relationships EMPTY = new Relationships(map());

    private final Map<String, Relationship> relationships;

    public Relationships(Map<String, Relationship> relationships) {
        this.relationships = relationships;
    }

    public Relationship findRelationshipById(String id) {
        return Optional.ofNullable(relationships.get(id)).get();
    }
}
