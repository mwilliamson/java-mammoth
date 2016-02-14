package org.zwobble.mammoth.documents;

import java.util.List;

public class Note {
    private final NoteType noteType;
    private final String id;
    private final List<DocumentElement> body;

    public Note(NoteType noteType, String id, List<DocumentElement> body) {
        this.noteType = noteType;
        this.id = id;
        this.body = body;
    }

    public NoteType getNoteType() {
        return noteType;
    }

    public String getId() {
        return id;
    }
}
