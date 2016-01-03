package org.zwobble.mammoth.docx;

import org.zwobble.mammoth.documents.Style;

import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.util.MammothMaps.map;

public class Styles {
    public static final Styles EMPTY = new Styles(map(), map());

    private final Map<String, Style> paragraphStyles;
    private final Map<String, Style> characterStyles;

    public Styles(Map<String, Style> paragraphStyles, Map<String, Style> characterStyles) {
        this.paragraphStyles = paragraphStyles;
        this.characterStyles = characterStyles;
    }

    public Optional<Style> findParagraphStyleById(String id) {
        return Optional.ofNullable(paragraphStyles.get(id));
    }

    public Optional<Style> findCharacterStyleById(String id) {
        return Optional.ofNullable(characterStyles.get(id));
    }
}
