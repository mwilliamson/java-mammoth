package org.zwobble.mammoth.internal.styles.parsing;

import org.zwobble.mammoth.internal.styles.HtmlPath;
import org.zwobble.mammoth.internal.styles.HtmlPathElement;
import org.zwobble.mammoth.internal.styles.HtmlPathElements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.zwobble.mammoth.internal.util.Maps.map;

public class HtmlPathParser {
    public static HtmlPath parse(TokenIterator tokens) {
        List<HtmlPathElement> elements = new ArrayList<>();

        if (tokens.peekTokenType() == TokenType.IDENTIFIER) {
            HtmlPathElement element = parseElement(tokens);
            elements.add(element);
            while (tokens.peekTokenType() == TokenType.WHITESPACE && tokens.peekTokenType(1) == TokenType.GREATER_THAN) {
                tokens.skip(TokenType.WHITESPACE);
                tokens.skip(TokenType.GREATER_THAN);
                tokens.skip(TokenType.WHITESPACE);
                elements.add(parseElement(tokens));
            }
        }

        return new HtmlPathElements(elements);
    }

    private static HtmlPathElement parseElement(TokenIterator tokens) {
        List<String> tagNames = parseTagNames(tokens);
        List<String> classNames = parseClassNames(tokens);
        Map<String, String> attributes = classNames.isEmpty()
            ? map()
            : map("class", String.join(" ", classNames));
        boolean isFresh = parseIsFresh(tokens);
        return new HtmlPathElement(tagNames, attributes, !isFresh);
    }

    private static List<String> parseTagNames(TokenIterator tokens) {
        List<String> tagNames = new ArrayList<>();
        tagNames.add(tokens.nextValue(TokenType.IDENTIFIER));
        while (tokens.peekTokenType() == TokenType.CHOICE) {
            tokens.skip();
            tagNames.add(tokens.nextValue(TokenType.IDENTIFIER));
        }
        return tagNames;
    }

    private static List<String> parseClassNames(TokenIterator tokens) {
        List<String> classNames = new ArrayList<>();
        while (tokens.peekTokenType() == TokenType.DOT) {
            tokens.skip();
            classNames.add(tokens.nextValue(TokenType.IDENTIFIER));
        }
        return classNames;
    }

    private static boolean parseIsFresh(TokenIterator tokens) {
        if (tokens.peekTokenType() == TokenType.COLON) {
            tokens.skip();
            tokens.skip(TokenType.IDENTIFIER, "fresh");
            return true;
        } else {
            return false;
        }
    }
}
