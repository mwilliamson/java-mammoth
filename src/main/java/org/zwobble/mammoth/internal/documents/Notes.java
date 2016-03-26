package org.zwobble.mammoth.internal.documents;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.*;

public class Notes {
    public final static Notes EMPTY = new Notes(list());

    private final Map<NoteType, Map<String, Note>> notes;

    public Notes(List<Note> notes) {
        this.notes = eagerMapValues(
            toMultiMapWithKey(notes, Note::getNoteType),
            notesOfType -> toMapWithKey(notesOfType, Note::getId));
    }

    public Optional<Note> findNote(NoteType noteType, String noteId) {
        return lookup(notes, noteType)
            .flatMap(notesOfType -> lookup(notesOfType, noteId));
    }
}
