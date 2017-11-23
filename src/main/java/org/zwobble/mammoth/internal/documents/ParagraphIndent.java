package org.zwobble.mammoth.internal.documents;

import java.util.Optional;

public class ParagraphIndent {
    private final Optional<String> start;
    private final Optional<String> end;
    private final Optional<String> firstLine;
    private final Optional<String> hanging;

    public ParagraphIndent(Optional<String> start, Optional<String> end, Optional<String> firstLine, Optional<String> hanging) {
        this.start = start;
        this.end = end;
        this.firstLine = firstLine;
        this.hanging = hanging;
    }

    public Optional<String> getStart() {
        return start;
    }

    public Optional<String> getEnd() {
        return end;
    }

    public Optional<String> getFirstLine() {
        return firstLine;
    }

    public Optional<String> getHanging() {
        return hanging;
    }
}
