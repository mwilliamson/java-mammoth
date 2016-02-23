package org.zwobble.mammoth.styles.parsing;

import org.parboiled.support.Var;
import org.zwobble.mammoth.styles.StyleMap;
import org.zwobble.mammoth.styles.StyleMapBuilder;

public class StyleMapParser {
    public static StyleMap parse(String input) {
        if (input.isEmpty()) {
            return StyleMap.EMPTY;
        } else {
            Var<StyleMapBuilder> styleMap = new Var<>(StyleMap.builder());
            Parsing.parse(StyleMappingParser.class, parser -> parser.StyleMapping(styleMap), input);
            return styleMap.get().build();
        }
    }
}
