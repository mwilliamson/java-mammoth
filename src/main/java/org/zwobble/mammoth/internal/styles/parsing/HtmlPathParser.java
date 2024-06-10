package org.zwobble.mammoth.internal.styles.parsing;

import org.zwobble.mammoth.internal.html.HtmlTag;
import org.zwobble.mammoth.internal.styles.HtmlPath;
import org.zwobble.mammoth.internal.styles.HtmlPathElement;
import org.zwobble.mammoth.internal.styles.HtmlPathElements;

import java.util.*;

import static org.zwobble.mammoth.internal.styles.parsing.TokenParser.parseIdentifier;
import static org.zwobble.mammoth.internal.styles.parsing.TokenParser.parseString;
import static org.zwobble.mammoth.internal.util.Maps.lookup;

public class HtmlPathParser {
    public static HtmlPath parse(TokenIterator<TokenType> tokens) {
        if (tokens.trySkip(TokenType.SYMBOL, "!")) {
            return HtmlPath.IGNORE;
        } else {
            return parseHtmlPathElements(tokens);
        }
    }

    private static class Attribute {
        private final String name;
        private final String value;
        private final boolean append;

        private Attribute(String name, String value, boolean append) {
            this.name = name;
            this.value = value;
            this.append = append;
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

        List<Attribute> attributeList = parseAttributeOrClassNames(tokens);
        Map<String, String> attributes = new HashMap<>();
        for (Attribute attribute : attributeList) {
            if (attribute.append && attributes.containsKey(attribute.name)) {
                attributes.put(
                    attribute.name,
                    lookup(attributes, attribute.name).get() + " " + attribute.value
                );
            } else {
                attributes.put(attribute.name, attribute.value);
            }
        }

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

    private static List<Attribute> parseAttributeOrClassNames(TokenIterator<TokenType> tokens) {
        List<Attribute> attributes = new ArrayList<>();
        while (true) {
            Optional<Attribute> attribute = parseAttributeOrClassName(tokens);
            if (attribute.isPresent()) {
                attributes.add(attribute.get());
            } else {
                return attributes;
            }
        }
    }

    private static Optional<Attribute> parseAttributeOrClassName(TokenIterator<TokenType> tokens) {
        if (tokens.isNext(TokenType.SYMBOL, "[")) {
            return Optional.of(parseAttribute(tokens));
        } else if (tokens.isNext(TokenType.SYMBOL, ".")) {
            return Optional.of(parseClassName(tokens));
        } else {
            return Optional.empty();
        }
    }

    private static Attribute parseAttribute(TokenIterator<TokenType> tokens) {
        tokens.skip(TokenType.SYMBOL, "[");
        String name = parseIdentifier(tokens);
        tokens.skip(TokenType.SYMBOL, "=");
        String value = parseString(tokens);
        tokens.skip(TokenType.SYMBOL, "]");
        return new Attribute(name, value, true);
    }

    private static Attribute parseClassName(TokenIterator<TokenType> tokens) {
        tokens.skip(TokenType.SYMBOL, ".");
        String className = parseIdentifier(tokens);
        return new Attribute("class", className, true);
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
