package org.zwobble.mammoth.internal.styles.parsing;

import org.zwobble.mammoth.internal.documents.NumberingLevel;
import org.zwobble.mammoth.internal.styles.*;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.BiConsumer;

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
            case "b":
                return StyleMapBuilder::bold;
            case "i":
                return StyleMapBuilder::italic;
            case "u":
                return StyleMapBuilder::underline;
            case "strike":
                return StyleMapBuilder::strikethrough;
            case "comment-reference":
                return StyleMapBuilder::commentReference;
            case "br":
                BreakMatcher breakMatcher = parseBreakMatcher(tokens);
                return (builder, path) -> builder.mapBreak(breakMatcher, path);
            default:
                throw new LineParseException(identifier, "Unrecognised document element: " + identifier);
        }
    }

    public static ParagraphMatcher parseParagraphMatcher(TokenIterator<TokenType> tokens) {
        Optional<String> styleId = parseStyleId(tokens);
        Optional<StringMatcher> styleName = parseStyleName(tokens);
        Optional<NumberingLevel> numbering = parseNumbering(tokens);
        return new ParagraphMatcher(styleId, styleName, numbering);
    }

    public static RunMatcher parseRunMatcher(TokenIterator<TokenType> tokens) {
        Optional<String> styleId = parseStyleId(tokens);
        Optional<StringMatcher> styleName = parseStyleName(tokens);
        return new RunMatcher(styleId, styleName);
    }

    private static Optional<String> parseStyleId(TokenIterator<TokenType> tokens) {
        return TokenParser.parseClassName(tokens);
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
            throw new LineParseException(tokens.next(), "Expected string matcher but got token " + tokens.next().getValue());
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
                throw new LineParseException(listType, "Unrecognised list type: " + listType);
        }
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
            default:
                throw new LineParseException(stringToken, "Unrecognised break type: " + typeName);
        }
    }
}
