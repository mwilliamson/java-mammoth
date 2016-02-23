package org.zwobble.mammoth.styles.parsing;

import org.parboiled.support.Var;
import org.zwobble.mammoth.styles.StyleMap;
import org.zwobble.mammoth.styles.StyleMapBuilder;

import java.util.List;

import static java.util.Arrays.asList;

public class StyleMapParser {
    public static StyleMap parse(String input) {
        return parse(asList(input.split("\\r?\\n")));
    }

    public static StyleMap parse(List<String> lines) {
        Var<StyleMapBuilder> styleMap = new Var<>(StyleMap.builder());
        for (String line : lines) {
            handleLine(styleMap, line);
        }
        return styleMap.get().build();
    }

    private static void handleLine(Var<StyleMapBuilder> styleMap, String line) {
        if (line.startsWith("#")) {
            return;
        }
        line = line.trim();
        if (line.isEmpty()) {
            return;
        }
        Parsing.parse(StyleMappingParser.class, parser -> parser.StyleMapping(styleMap), line);
    }
}
