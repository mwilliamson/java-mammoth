package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.documents.Style;

import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.Maps.lookup;
import static org.zwobble.mammoth.internal.util.Maps.map;

public class Styles {
    public static final Styles EMPTY = new Styles(map(), map(), map());

    private final Map<String, Style> paragraphStyles;
    private final Map<String, Style> characterStyles;
    private final Map<String, Style> tableStyles;

    public Styles(Map<String, Style> paragraphStyles, Map<String, Style> characterStyles, Map<String, Style> tableStyles) {
        this.paragraphStyles = paragraphStyles;
        this.characterStyles = characterStyles;
        this.tableStyles = tableStyles;
    }

    public Optional<Style> findParagraphStyleById(String id) {
        return lookup(paragraphStyles, id);
    }

    public Optional<Style> findCharacterStyleById(String id) {
        return lookup(characterStyles, id);
    }

    public Optional<Style> findTableStyleById(String id) {
        return lookup(tableStyles, id);
    }
}
