package org.zwobble.mammoth.internal.styles;

import org.zwobble.mammoth.internal.documents.Table;

import java.util.Optional;

public class TableMatcher implements DocumentElementMatcher<Table> {
    public static final TableMatcher ANY = new TableMatcher(Optional.empty(), Optional.empty());

    public static TableMatcher styleId(String styleId) {
        return new TableMatcher(Optional.of(styleId), Optional.empty());
    }

    public static TableMatcher styleName(String styleName) {
        return new TableMatcher(Optional.empty(), Optional.of(new EqualToStringMatcher(styleName)));
    }

    private final Optional<String> styleId;
    private final Optional<StringMatcher> styleName;

    public TableMatcher(Optional<String> styleId, Optional<StringMatcher> styleName) {
        this.styleId = styleId;
        this.styleName = styleName;
    }

    @Override
    public boolean matches(Table table) {
        return DocumentElementMatching.matchesStyle(styleId, styleName, table.getStyle());
    }
}
