package org.zwobble.mammoth.internal.styles.parsing;

import java.util.List;

import static org.zwobble.mammoth.internal.util.Lists.list;

public class StyleMappingTokeniser {
    public static List<Token<TokenType>> tokenise(String line) {
        String stringPrefix = "'((?:\\.|[^'])*)";

        RegexTokeniser<TokenType> tokeniser = new RegexTokeniser<>(
            TokenType.UNKNOWN,
            list(
                RegexTokeniser.rule(TokenType.IDENTIFIER, "([a-zA-Z][a-zA-Z0-9\\-]*)"),
                RegexTokeniser.rule(TokenType.CLASS_NAME, "(\\.(?:[a-zA-Z0-9\\-]|\\\\.)+)"),
                RegexTokeniser.rule(TokenType.SYMBOL, ":|>|=>|=|\\(|\\)|\\[|\\]|\\||!"),
                RegexTokeniser.rule(TokenType.WHITESPACE, "\\s+"),
                RegexTokeniser.rule(TokenType.STRING, stringPrefix + "'"),
                RegexTokeniser.rule(TokenType.UNTERMINATED_STRING, stringPrefix),
                RegexTokeniser.rule(TokenType.INTEGER, "([0-9]+)")
            )
        );
        List<Token<TokenType>> tokens = tokeniser.tokenise(line);
        tokens.add(new Token<>(line.length(), TokenType.EOF, ""));
        return tokens;
    }
}
