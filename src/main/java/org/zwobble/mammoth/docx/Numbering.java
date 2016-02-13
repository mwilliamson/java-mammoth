package org.zwobble.mammoth.docx;

import org.zwobble.mammoth.documents.NumberingLevel;

import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.util.MammothMaps.map;

public class Numbering {
    public static final Numbering EMPTY = new Numbering(map());

    private final Map<String, Map<String, NumberingLevel>> numbering;

    public Numbering(Map<String, Map<String, NumberingLevel>> numbering) {
        this.numbering = numbering;
    }

    public Optional<NumberingLevel> findLevel(String numId, String level) {
        return Optional.ofNullable(numbering.get(numId))
            .flatMap(levels -> Optional.ofNullable(levels.get(level)));
    }
}
