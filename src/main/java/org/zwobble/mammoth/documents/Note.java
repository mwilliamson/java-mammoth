package org.zwobble.mammoth.documents;

import java.util.List;

public class Note {
    private final String id;
    private final List<DocumentElement> body;

    public Note(String id, List<DocumentElement> body) {
        this.id = id;
        this.body = body;
    }
}
