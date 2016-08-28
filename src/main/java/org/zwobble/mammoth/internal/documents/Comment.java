package org.zwobble.mammoth.internal.documents;

import java.util.List;
import java.util.Optional;

public class Comment {
    private final String commentId;
    private final List<DocumentElement> body;
    private final Optional<String> authorName;
    private final Optional<String> authorInitials;

    public Comment(String commentId, List<DocumentElement> body, Optional<String> authorName, Optional<String> authorInitials) {
        this.commentId = commentId;
        this.body = body;
        this.authorName = authorName;
        this.authorInitials = authorInitials;
    }

    public String getCommentId() {
        return commentId;
    }

    public List<DocumentElement> getBody() {
        return body;
    }

    public Optional<String> getAuthorInitials() {
        return authorInitials;
    }

    public Optional<String> getAuthorName() {
        return authorName;
    }
}
