package org.zwobble.mammoth.tests.docx;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.docx.Relationships;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.tests.util.MammothAsserts.assertThrows;

public class RelationshipsTests {
    @Test
    public void exceptionIsThrownIfRelationshipCannotBeFound() {
        Relationships relationships = new Relationships(map());
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> relationships.findRelationshipById("rId5"));
        assertEquals("Could not find relationship 'rId5'", exception.getMessage());
    }
}
