package org.zwobble.mammoth.internal.styles.parsing;

import org.zwobble.mammoth.internal.styles.HtmlPath;
import org.zwobble.mammoth.internal.styles.StyleMap;
import org.zwobble.mammoth.internal.styles.StyleMapBuilder;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.Arrays.asList;

public class StyleMapParser {
    public static StyleMap parse(String input) {
        return parseStyleMappings(asList(input.split("\\r?\\n")));
    }

    public static StyleMap parseStyleMappings(List<String> lines) {
        StyleMapBuilder styleMap = StyleMap.builder();
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex += 1) {
            try {
                handleLine(styleMap, lines.get(lineIndex));
            } catch (LineParseException exception) {
                throw new ParseException(lineIndex + 1, exception.getToken(), exception.getMessage());
            }
        }
        return styleMap.build();
    }

    private static void handleLine(StyleMapBuilder styleMap, String line) {
        if (line.startsWith("#")) {
            return;
        }
        line = line.trim();
        if (line.isEmpty()) {
            return;
        }

        parseStyleMapping(line).accept(styleMap);
    }

    private static Consumer<StyleMapBuilder> parseStyleMapping(String line) {
        TokenIterator tokens = new TokenIterator(StyleMappingTokeniser.tokenise(line));

        BiConsumer<StyleMapBuilder, HtmlPath> documentMatcher = DocumentMatcherParser.parse(tokens);

        tokens.skip(TokenType.WHITESPACE);
        tokens.skip(TokenType.ARROW);

        HtmlPath htmlPath = parseHtmlPath(tokens);

        tokens.skip(TokenType.EOF);

        return styleMap -> documentMatcher.accept(styleMap, htmlPath);
    }

    private static HtmlPath parseHtmlPath(TokenIterator tokens) {
        if (tokens.peekTokenType() == TokenType.EOF) {
            return HtmlPath.EMPTY;
        } else {
            tokens.skip(TokenType.WHITESPACE);
            return HtmlPathParser.parse(tokens);
        }
    }
}
