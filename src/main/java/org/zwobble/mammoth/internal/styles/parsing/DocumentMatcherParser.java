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
            tokens.skip(TokenType.SYMBOL, "=");
            String value = TokenParser.parseString(tokens);
            tokens.skip(TokenType.SYMBOL, "]");
            return Optional.of(new EqualToStringMatcher(value));
        } else {
            return Optional.empty();
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
}
