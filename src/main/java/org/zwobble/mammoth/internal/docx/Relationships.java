package org.zwobble.mammoth.internal.docx;

import java.util.Map;

import static org.zwobble.mammoth.internal.util.Maps.lookup;
import static org.zwobble.mammoth.internal.util.Maps.map;

public class Relationships {
    public static final Relationships EMPTY = new Relationships(map());

    private final Map<String, Relationship> relationships;

    public Relationships(Map<String, Relationship> relationships) {
        this.relationships = relationships;
    }

    public Relationship findRelationshipById(String id) {
        return lookup(relationships, id)
            .orElseThrow(() -> new RuntimeException("Could not find relationship '" + id + "'"));
    }
}
