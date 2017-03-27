package org.zwobble.mammoth.internal.styles.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static org.zwobble.mammoth.internal.util.Iterables.*;
import static org.zwobble.mammoth.internal.util.Lists.eagerMap;

public class RegexTokeniser<T> {
    public static <T> TokenRule<T> rule(T type, String regex) {
        return new TokenRule<>(type, Pattern.compile(regex));
    }

    public static class TokenRule<T> {
        private final T type;
        private final Pattern regex;

        public TokenRule(T type, Pattern regex) {
            if (regex.matcher("").groupCount() != 0) {
                throw new RuntimeException("regex cannot contain any groups");
            }

            this.type = type;
            this.regex = regex;
        }
    }

    private final Pattern pattern;
    private final List<T> rules;

    public RegexTokeniser(T unknown, List<TokenRule<T>> rules) {
        List<TokenRule<T>> allRules = new ArrayList<>(rules);
        allRules.add(rule(unknown, "."));
        this.pattern = Pattern.compile(String.join("|", lazyMap(allRules, rule -> "(" + rule.regex.pattern() + ")")));
        this.rules = eagerMap(allRules, rule -> rule.type);
    }

    public List<Token<T>> tokenise(String value) {
        Matcher matcher = pattern.matcher(value);
        List<Token<T>> tokens = new ArrayList<>();
        while (matcher.lookingAt()) {
            Optional<Integer> groupIndex = tryFind(intRange(0, this.rules.size()), index -> !isNull(matcher.group(index + 1)));
            if (groupIndex.isPresent()) {
                T tokenType = this.rules.get(groupIndex.get());
                tokens.add(new Token<>(matcher.regionStart(), tokenType, matcher.group()));
                matcher.region(matcher.end(), value.length());
            } else {
                // Should be impossible
                throw new RuntimeException("Could not find group");
            }
        }
        return tokens;
    }


}
