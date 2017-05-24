package org.zwobble.mammoth.internal.documents;

public class CommentReference implements DocumentElement {
    private final String commentId;

    public CommentReference(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentId() {
        return commentId;
    }

    @Override
    public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
        return visitor.visit(this, context);
    }
}
