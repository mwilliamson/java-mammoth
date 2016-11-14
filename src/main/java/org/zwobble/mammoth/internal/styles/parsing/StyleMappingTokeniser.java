package org.zwobble.mammoth.internal.styles.parsing;

import java.util.ArrayList;
import java.util.List;

public class StyleMappingTokeniser {
    public static List<Token> tokenise(String line) {
        List<Token> tokens = new ArrayList<>();

        int index = 0;
        while (index < line.length()) {
            Token token = nextToken(line, index);
            tokens.add(token);
            index += token.getValue().length();
        }

        tokens.add(new Token(index, TokenType.EOF, ""));
        return tokens;
    }

    private static Token nextToken(String line, int startIndex) {
        char nextChar = line.charAt(startIndex);
        TokenType tokenType = tokenType(nextChar);
        int index = 0;
        if (nextChar == '=' && startIndex + 1 < line.length() && line.charAt(startIndex + 1) == '>') {
            tokenType = TokenType.ARROW;
            index = startIndex + 2;
        } else {
            index = seekEndOfToken(line, startIndex + 1, tokenType);
            if (tokenType == TokenType.STRING && index == line.length() + 1) {
                tokenType = TokenType.UNTERMINATED_STRING;
            }
        }
        index = Math.min(index, line.length());
        return new Token(startIndex, tokenType, line.substring(startIndex, index));
    }

    private static TokenType tokenType(char nextChar) {
        if (isAlphabetic(nextChar)) {
            return TokenType.IDENTIFIER;
        } else if (isWhitespace(nextChar)) {
            return TokenType.WHITESPACE;
        } else if (isNumeric(nextChar)) {
            return TokenType.INTEGER;
        } else {
            switch (nextChar) {
                case '\'':
                    return TokenType.STRING;
                case '.':
                    return TokenType.DOT;
                case ':':
                    return TokenType.COLON;
                case '>':
                    return TokenType.GREATER_THAN;
                case '=':
                    return TokenType.EQUALS;
                case '(':
                    return TokenType.OPEN_PAREN;
                case ')':
                    return TokenType.CLOSE_PAREN;
                case '[':
                    return TokenType.OPEN_SQUARE_BRACKET;
                case ']':
                    return TokenType.CLOSE_SQUARE_BRACKET;
                case '|':
                    return TokenType.CHOICE;
                case '!':
                    return TokenType.BANG;
                default:
                    return TokenType.UNKNOWN;
            }
        }
    }

    private static int seekEndOfToken(String line, int index, TokenType tokenType) {
        switch (tokenType) {
            case WHITESPACE:
                while (index < line.length() && isWhitespace(line.charAt(index))) {
                    index += 1;
                }
                return index;
            case IDENTIFIER:
                while (index < line.length() && isIdentifierCharacter(line.charAt(index))) {
                    index += 1;
                }
                return index;
            case INTEGER:
                while (index < line.length() && isNumeric(line.charAt(index))) {
                    index += 1;
                }
                return index;
            case STRING:
                while (index < line.length() && line.charAt(index) != '\'') {
                    index += 1;
                }
                index += 1;
                return index;
            default:
                return index;
        }
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t';
    }

    private static boolean isIdentifierCharacter(char c) {
        return isAlphabetic(c) || isNumeric(c) || c == '-' || c == '_';
    }

    private static boolean isAlphabetic(char nextChar) {
        return (nextChar >= 'a' && nextChar <= 'z') || (nextChar >= 'A' && nextChar <= 'Z');
    }

    private static boolean isNumeric(char c) {
        return c >= '0' && c <= '9';
    }
}
