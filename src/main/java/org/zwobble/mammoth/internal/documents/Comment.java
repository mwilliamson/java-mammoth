package org.zwobble.mammoth.internal.documents;

import java.util.List;

public class Comment {
    private final String commentId;
    private final List<DocumentElement> body;

    public Comment(String commentId, List<DocumentElement> body) {
        this.commentId = commentId;
        this.body = body;
    }

    public String getCommentId() {
        return commentId;
    }

    public List<DocumentElement> getBody() {
        return body;
    }
}
