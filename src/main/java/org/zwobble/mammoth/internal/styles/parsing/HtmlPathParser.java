package org.zwobble.mammoth.internal.styles.parsing;

import org.zwobble.mammoth.internal.html.HtmlTag;
import org.zwobble.mammoth.internal.styles.HtmlPath;
import org.zwobble.mammoth.internal.styles.HtmlPathElement;
import org.zwobble.mammoth.internal.styles.HtmlPathElements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.internal.styles.parsing.TokenParser.parseIdentifier;
import static org.zwobble.mammoth.internal.util.Maps.map;

public class HtmlPathParser {
    public static HtmlPath parse(TokenIterator<TokenType> tokens) {
        if (tokens.trySkip(TokenType.SYMBOL, "!")) {
            return HtmlPath.IGNORE;
        } else {
            return parseHtmlPathElements(tokens);
        }
    }

    private static HtmlPath parseHtmlPathElements(TokenIterator<TokenType> tokens) {
        List<HtmlPathElement> elements = new ArrayList<>();

        if (tokens.peekTokenType() == TokenType.IDENTIFIER) {
            HtmlPathElement element = parseElement(tokens);
            elements.add(element);
            while (tokens.peekTokenType() == TokenType.WHITESPACE && tokens.isNext(1, TokenType.SYMBOL, ">")) {
                tokens.skip(TokenType.WHITESPACE);
                tokens.skip(TokenType.SYMBOL, ">");
                tokens.skip(TokenType.WHITESPACE);
                elements.add(parseElement(tokens));
            }
        }

        return new HtmlPathElements(elements);
    }

    private static HtmlPathElement parseElement(TokenIterator<TokenType> tokens) {
        List<String> tagNames = parseTagNames(tokens);
        List<String> classNames = parseClassNames(tokens);
        Map<String, String> attributes = classNames.isEmpty()
            ? map()
            : map("class", String.join(" ", classNames));
        boolean isFresh = parseIsFresh(tokens);
        String separator = parseSeparator(tokens);
        return new HtmlPathElement(new HtmlTag(tagNames, attributes, !isFresh, separator));
    }

    private static List<String> parseTagNames(TokenIterator<TokenType> tokens) {
        List<String> tagNames = new ArrayList<>();
        tagNames.add(parseIdentifier(tokens));
        while (tokens.trySkip(TokenType.SYMBOL, "|")) {
            tagNames.add(parseIdentifier(tokens));
        }
        return tagNames;
    }

    private static List<String> parseClassNames(TokenIterator<TokenType> tokens) {
        List<String> classNames = new ArrayList<>();
        while (true) {
            Optional<String> className = TokenParser.parseClassName(tokens);
            if (className.isPresent()) {
                classNames.add(className.get());
            } else {
                return classNames;
            }
        }
    }

    private static boolean parseIsFresh(TokenIterator<TokenType> tokens) {
        return tokens.tryParse(() -> {
            tokens.skip(TokenType.SYMBOL, ":");
            tokens.skip(TokenType.IDENTIFIER, "fresh");
        });
    }

    private static String parseSeparator(TokenIterator<TokenType> tokens) {
        boolean isSeparator = tokens.tryParse(() -> {
            tokens.skip(TokenType.SYMBOL, ":");
            tokens.skip(TokenType.IDENTIFIER, "separator");
        });
        if (isSeparator) {
            tokens.skip(TokenType.SYMBOL, "(");
            String value = TokenParser.parseString(tokens);
            tokens.skip(TokenType.SYMBOL, ")");
            return value;
        } else {
            return "";
        }
    }
}
