package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.documents.Style;

import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.MammothMaps.lookup;
import static org.zwobble.mammoth.internal.util.MammothMaps.map;

public class Styles {
    public static final Styles EMPTY = new Styles(map(), map());

    private final Map<String, Style> paragraphStyles;
    private final Map<String, Style> characterStyles;

    public Styles(Map<String, Style> paragraphStyles, Map<String, Style> characterStyles) {
        this.paragraphStyles = paragraphStyles;
        this.characterStyles = characterStyles;
    }

    public Optional<Style> findParagraphStyleById(String id) {
        return lookup(paragraphStyles, id);
    }

    public Optional<Style> findCharacterStyleById(String id) {
        return lookup(characterStyles, id);
    }
}
