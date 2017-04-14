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
            String line = lines.get(lineIndex);
            try {
                handleLine(styleMap, line);
            } catch (LineParseException exception) {
                throw new ParseException(generateErrorMessage(line, lineIndex + 1, exception.getCharacterIndex(), exception.getMessage()));
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
        TokenIterator<TokenType> tokens = StyleMappingTokeniser.tokenise(line);

        BiConsumer<StyleMapBuilder, HtmlPath> documentMatcher = DocumentMatcherParser.parse(tokens);

        tokens.skip(TokenType.WHITESPACE);
        tokens.skip(TokenType.SYMBOL, "=>");

        HtmlPath htmlPath = parseHtmlPath(tokens);

        tokens.skip(TokenType.EOF);

        return styleMap -> documentMatcher.accept(styleMap, htmlPath);
    }

    private static HtmlPath parseHtmlPath(TokenIterator<TokenType> tokens) {
        if (tokens.peekTokenType() == TokenType.EOF) {
            return HtmlPath.EMPTY;
        } else {
            tokens.skip(TokenType.WHITESPACE);
            return HtmlPathParser.parse(tokens);
        }
    }

    private static String generateErrorMessage(String line, int lineNumber, int characterIndex, String message) {
        return "error reading style map at line " + lineNumber + ", character " + (characterIndex + 1) +
            ": " + message + "\n\n" +
            line + "\n" +
            repeatString(" ", characterIndex) + "^";
    }

    private static String repeatString(String value, int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; i += 1) {
            builder.append(value);
        }
        return builder.toString();
    }
}
