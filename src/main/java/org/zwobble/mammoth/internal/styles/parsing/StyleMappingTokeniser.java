package org.zwobble.mammoth.internal.styles.parsing;

import java.util.List;

import static org.zwobble.mammoth.internal.util.Lists.list;

public class StyleMappingTokeniser {
    public static List<Token<TokenType>> tokenise(String line) {
        String stringPrefix = "'((?:\\.|[^'])*)";

        RegexTokeniser<TokenType> tokeniser = new RegexTokeniser<>(
            list(
                RegexTokeniser.rule(TokenType.IDENTIFIER, "([a-zA-Z][a-zA-Z0-9\\-]*)"),
                RegexTokeniser.rule(TokenType.CLASS_NAME, "(\\.(?:[a-zA-Z0-9\\-]|\\\\.)+)"),
                RegexTokeniser.rule(TokenType.COLON, ":"),
                RegexTokeniser.rule(TokenType.GREATER_THAN, ">"),
                RegexTokeniser.rule(TokenType.WHITESPACE, "\\s+"),
                RegexTokeniser.rule(TokenType.ARROW, "=>"),
                RegexTokeniser.rule(TokenType.EQUALS, "="),
                RegexTokeniser.rule(TokenType.OPEN_PAREN, "\\("),
                RegexTokeniser.rule(TokenType.CLOSE_PAREN, "\\)"),
                RegexTokeniser.rule(TokenType.OPEN_SQUARE_BRACKET, "\\["),
                RegexTokeniser.rule(TokenType.CLOSE_SQUARE_BRACKET, "\\]"),
                RegexTokeniser.rule(TokenType.STRING, stringPrefix + "'"),
                RegexTokeniser.rule(TokenType.UNTERMINATED_STRING, stringPrefix),
                RegexTokeniser.rule(TokenType.INTEGER, "([0-9]+)"),
                RegexTokeniser.rule(TokenType.CHOICE, "\\|"),
                RegexTokeniser.rule(TokenType.BANG, "!")
            )
        );
        List<Token<TokenType>> tokens = tokeniser.tokenise(line);
        tokens.add(new Token<>(line.length(), TokenType.EOF, ""));
        return tokens;
    }
}
