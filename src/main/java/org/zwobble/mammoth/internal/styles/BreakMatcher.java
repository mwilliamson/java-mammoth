package org.zwobble.mammoth.internal.styles;

import org.zwobble.mammoth.internal.documents.Break;

public class BreakMatcher implements DocumentElementMatcher<Break> {
    public static final BreakMatcher LINE_BREAK = new BreakMatcher(Break.Type.LINE);
    public static final BreakMatcher PAGE_BREAK = new BreakMatcher(Break.Type.PAGE);

    private final Break.Type breakType;

    private BreakMatcher(Break.Type breakType) {
        this.breakType = breakType;
    }

    @Override
    public boolean matches(Break element) {
        return element.getType().equals(breakType);
    }
}
