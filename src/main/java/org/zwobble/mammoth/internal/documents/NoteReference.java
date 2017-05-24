package org.zwobble.mammoth.internal.documents;

public class NoteReference implements DocumentElement {
    public static NoteReference footnoteReference(String noteId) {
        return new NoteReference(NoteType.FOOTNOTE, noteId);
    }

    public static NoteReference endnoteReference(String noteId) {
        return new NoteReference(NoteType.ENDNOTE, noteId);
    }

    private final NoteType noteType;
    private final String noteId;

    public NoteReference(NoteType noteType, String noteId) {
        this.noteType = noteType;
        this.noteId = noteId;
    }

    public NoteType getNoteType() {
        return noteType;
    }

    public String getNoteId() {
        return noteId;
    }

    @Override
    public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
        return visitor.visit(this, context);
    }
}
