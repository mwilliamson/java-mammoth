package org.zwobble.mammoth.internal.documents;

public interface DocumentElementVisitor<T, U> {
    T visit(Document document, U context);

    T visit(Paragraph paragraph, U context);
    T visit(Run run, U context);
    T visit(Text text, U context);

    T visit(Tab tab, U context);
    T visit(Break lineBreak, U context);

    T visit(Table table, U context);
    T visit(TableRow tableRow, U context);
    T visit(TableCell tableCell, U context);

    T visit(Hyperlink hyperlink, U context);
    T visit(Checkbox checkbox, U context);
    T visit(Bookmark bookmark, U context);
    T visit(NoteReference noteReference, U context);
    T visit(CommentReference commentReference, U context);

    T visit(Image image, U context);

}
