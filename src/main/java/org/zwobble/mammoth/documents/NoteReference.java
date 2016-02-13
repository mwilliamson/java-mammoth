package org.zwobble.mammoth.documents;

public class NoteReference implements DocumentElement {
    public static NoteReference footnoteReference(String noteId) {
        return new NoteReference(noteId);
    }

    private final String noteId;

    public NoteReference(String noteId) {
        this.noteId = noteId;
    }

    @Override
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
