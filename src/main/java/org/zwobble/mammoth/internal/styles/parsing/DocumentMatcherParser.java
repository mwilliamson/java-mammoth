package org.zwobble.mammoth.internal.styles.parsing;

import org.zwobble.mammoth.internal.documents.NumberingLevel;
import org.zwobble.mammoth.internal.styles.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.zwobble.mammoth.internal.styles.parsing.LineParseException.lineParseException;
import static org.zwobble.mammoth.internal.util.Lists.list;

public class DocumentMatcherParser {
    public static BiConsumer<StyleMapBuilder, HtmlPath> parse(TokenIterator<TokenType> tokens) {
        Token<TokenType> identifier = tokens.next(TokenType.IDENTIFIER);
        switch (identifier.getValue()) {
            case "p":
                ParagraphMatcher paragraph = parseParagraphMatcher(tokens);
                return (builder, path) -> builder.mapParagraph(paragraph, path);
            case "r":
                RunMatcher run = parseRunMatcher(tokens);
                return (builder, path) -> builder.mapRun(run, path);
            case "table":
                TableMatcher table = parseTableMatcher(tokens);
                return (builder, path) -> builder.mapTable(table, path);
            case "b":
                return StyleMapBuilder::bold;
            case "i":
                return StyleMapBuilder::italic;
            case "u":
                return StyleMapBuilder::underline;
            case "strike":
                return StyleMapBuilder::strikethrough;
            case "all-caps":
                return StyleMapBuilder::allCaps;
            case "small-caps":
                return StyleMapBuilder::smallCaps;
            case "highlight":
                HighlightMatcher highlight = parseHighlightMatcher(tokens);
                return (builder, path) -> builder.mapHighlight(highlight, path);
            case "comment-reference":
                return StyleMapBuilder::commentReference;
            case "br":
                BreakMatcher breakMatcher = parseBreakMatcher(tokens);
                return (builder, path) -> builder.mapBreak(breakMatcher, path);
            default:
                throw lineParseException(identifier, "Unrecognised document element: " + identifier);
        }
    }

    private static ParagraphMatcher parseParagraphMatcher(TokenIterator<TokenType> tokens) {
        Optional<String> styleId = parseStyleId(tokens);
        Function<String, Optional<StringMatcher>> options = parseOptions(tokens, list("style-name", "text-align"));
        Optional<StringMatcher> styleName = options.apply("style-name");
        Optional<StringMatcher> textAlign = options.apply("text-align");
        Optional<NumberingLevel> numbering = parseNumbering(tokens);
        return new ParagraphMatcher(styleId, styleName, numbering, textAlign);
    }

    private static RunMatcher parseRunMatcher(TokenIterator<TokenType> tokens) {
        Optional<String> styleId = parseStyleId(tokens);
        Optional<StringMatcher> styleName = parseStyleName(tokens);
        return new RunMatcher(styleId, styleName);
    }

    private static TableMatcher parseTableMatcher(TokenIterator<TokenType> tokens) {
        Optional<String> styleId = parseStyleId(tokens);
        Optional<StringMatcher> styleName = parseStyleName(tokens);
        return new TableMatcher(styleId, styleName);
    }

    private static Optional<String> parseStyleId(TokenIterator<TokenType> tokens) {
        return TokenParser.parseClassName(tokens);
    }

    private static Function<String, Optional<StringMatcher>> parseOptions(TokenIterator<TokenType> tokens, List<String> properties) {
        if (tokens.trySkip(TokenType.SYMBOL, "[")) {
            Map<String, StringMatcher> options = new HashMap<String, StringMatcher>();
            Function<String, Optional<StringMatcher>> result = property -> options.containsKey(property) ? Optional.of(options.get(property)) : Optional.empty();
            while (tokens.peekTokenType() == TokenType.IDENTIFIER) {
                String identifier = tokens.nextValue(TokenType.IDENTIFIER);
                if (properties.contains(identifier)) {
                    StringMatcher stringMatcher = parseStringMatcher(tokens);
                    options.put(identifier, stringMatcher);
                }
                else {
                    throw lineParseException(tokens.next(), "Expected " + String.join(" or ", properties) + " but got token " + tokens.next().getValue());
                }
                if (tokens.peekTokenType() == TokenType.WHITESPACE) tokens.skip(TokenType.WHITESPACE);
                if (tokens.isNext(TokenType.SYMBOL, ",")) tokens.skip(TokenType.SYMBOL, ",");
                else {
                    // can not use break because couscous fails converting it.
                    tokens.skip(TokenType.SYMBOL, "]");
                    return result;
                }
                if (tokens.peekTokenType() == TokenType.WHITESPACE) tokens.skip(TokenType.WHITESPACE);
            }
            tokens.skip(TokenType.SYMBOL, "]");
            return result;
        } else {
            return property -> Optional.empty();
        }
    }

    private static Optional<StringMatcher> parseStyleName(TokenIterator<TokenType> tokens) {
        if (tokens.trySkip(TokenType.SYMBOL, "[")) {
            tokens.skip(TokenType.IDENTIFIER, "style-name");
            StringMatcher stringMatcher = parseStringMatcher(tokens);
            tokens.skip(TokenType.SYMBOL, "]");
            return Optional.of(stringMatcher);
        } else {
            return Optional.empty();
        }
    }

    private static StringMatcher parseStringMatcher(TokenIterator<TokenType> tokens) {
        if (tokens.trySkip(TokenType.SYMBOL, "=")) {
            return new EqualToStringMatcher(TokenParser.parseString(tokens));
        } else if (tokens.trySkip(TokenType.SYMBOL, "^=")) {
            return new StartsWithStringMatcher(TokenParser.parseString(tokens));
        } else {
            throw lineParseException(tokens.next(), "Expected string matcher but got token " + tokens.next().getValue());
        }
    }

    private static Optional<NumberingLevel> parseNumbering(TokenIterator<TokenType> tokens) {
        if (tokens.trySkip(TokenType.SYMBOL, ":")) {
            boolean isOrdered = parseListType(tokens);
            tokens.skip(TokenType.SYMBOL, "(");
            String level = new BigInteger(tokens.nextValue(TokenType.INTEGER)).subtract(BigInteger.ONE).toString();
            tokens.skip(TokenType.SYMBOL, ")");
            return Optional.of(new NumberingLevel(level, isOrdered));
        } else {
            return Optional.empty();
        }
    }

    private static boolean parseListType(TokenIterator<TokenType> tokens) {
        Token<TokenType> listType = tokens.next(TokenType.IDENTIFIER);
        switch (listType.getValue()) {
            case "ordered-list":
                return true;
            case "unordered-list":
                return false;
            default:
                throw lineParseException(listType, "Unrecognised list type: " + listType);
        }
    }

    private static HighlightMatcher parseHighlightMatcher(TokenIterator<TokenType> tokens) {
        Optional<String> color = Optional.empty();

        if (tokens.trySkip(TokenType.SYMBOL, "[")) {
            tokens.skip(TokenType.IDENTIFIER, "color");
            tokens.skip(TokenType.SYMBOL, "=");
            color = Optional.of(TokenParser.parseString(tokens));
            tokens.skip(TokenType.SYMBOL, "]");
        } else {
            color = Optional.empty();
        }

        return new HighlightMatcher(color);
    }

    private static BreakMatcher parseBreakMatcher(TokenIterator<TokenType> tokens) {
        tokens.skip(TokenType.SYMBOL, "[");
        tokens.skip(TokenType.IDENTIFIER, "type");
        tokens.skip(TokenType.SYMBOL, "=");
        Token<TokenType> stringToken = tokens.next(TokenType.STRING);
        tokens.skip(TokenType.SYMBOL, "]");
        String typeName = TokenParser.parseStringToken(stringToken);
        switch (typeName) {
            case "line":
                return BreakMatcher.LINE_BREAK;
            case "page":
                return BreakMatcher.PAGE_BREAK;
            case "column":
                return BreakMatcher.COLUMN_BREAK;
            default:
                throw lineParseException(stringToken, "Unrecognised break type: " + typeName);
        }
    }
}
