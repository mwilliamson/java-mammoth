package org.zwobble.mammoth.internal.styles.parsing;

import org.parboiled.support.Var;
import org.zwobble.mammoth.internal.styles.StyleMap;
import org.zwobble.mammoth.internal.styles.StyleMapBuilder;

import java.util.List;

import static java.util.Arrays.asList;

public class StyleMapParser {
    public static StyleMap parse(String input) {
        return parseStyleMappings(asList(input.split("\\r?\\n")));
    }

    public static StyleMap parseStyleMappings(List<String> lines) {
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
