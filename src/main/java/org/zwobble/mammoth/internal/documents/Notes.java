package org.zwobble.mammoth.internal.documents;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.MammothLists.list;

public class Notes {
    public final static Notes EMPTY = new Notes(list());

    private final Map<NoteType, Map<String, Note>> notes;

    public Notes(List<Note> notes) {
        this.notes = Maps.transformValues(
            Multimaps.index(notes, Note::getNoteType).asMap(),
            notesOfType -> Maps.uniqueIndex(notesOfType, Note::getId));
    }

    public Optional<Note> findNote(NoteType noteType, String noteId) {
        return Optional.ofNullable(notes.get(noteType))
            .flatMap(notesOfType -> Optional.ofNullable(notesOfType.get(noteId)));
    }
}
