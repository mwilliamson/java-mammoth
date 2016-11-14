package org.zwobble.mammoth.internal.styles.parsing;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTokeniser<T> {
    public static <T> TokenRule<T> rule(T type, String regex) {
        return new TokenRule<>(type, Pattern.compile(regex));
    }

    public static class TokenRule<T> {
        private final T type;
        private final Pattern regex;

        public TokenRule(T type, Pattern regex) {
            this.type = type;
            this.regex = regex;
        }
    }

    private final List<TokenRule<T>> rules;

    public RegexTokeniser(List<TokenRule<T>> rules) {
        this.rules = rules;
    }

    public List<Token<T>> tokenise(String value) {
        List<Token<T>> tokens = new ArrayList<>();
        CharBuffer remaining = CharBuffer.wrap(value);
        while (remaining.hasRemaining()) {
            boolean matched = false;
            for (TokenRule<T> rule : rules) {
                Matcher matcher = rule.regex.matcher(remaining);
                if (matcher.lookingAt()) {
                    tokens.add(new Token<>(remaining.position(), rule.type, matcher.group()));
                    remaining.position(remaining.position() + matcher.end());
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                throw new RuntimeException("Remaining: " + remaining);
            }
        }
        return tokens;
    }


}
