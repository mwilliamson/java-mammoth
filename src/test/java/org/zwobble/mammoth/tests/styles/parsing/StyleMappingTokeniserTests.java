package org.zwobble.mammoth.tests.styles.parsing;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.zwobble.mammoth.internal.styles.parsing.StyleMappingTokeniser;
import org.zwobble.mammoth.internal.styles.parsing.Token;
import org.zwobble.mammoth.internal.styles.parsing.TokenType;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.zwobble.mammoth.internal.util.Lists.eagerConcat;
import static org.zwobble.mammoth.internal.util.Lists.list;

public class StyleMappingTokeniserTests {
    @Test
    public void unknownTokensAreTokenised() {
        assertTokens("~", isToken(TokenType.UNKNOWN, "~"));
    }

    @Test
    public void emptyStringIsTokenisedToEndOfFileToken() {
        assertTokens("");
    }

    @Test
    public void whitespaceIsTokenised() {
        assertTokens(" \t\t  ", isToken(TokenType.WHITESPACE, " \t\t  "));
    }

    @Test
    public void identifiersAreTokenised() {
        assertTokens("Overture", isToken(TokenType.IDENTIFIER, "Overture"));
    }

    @Test
    public void integersAreTokenised() {
        assertTokens("123", isToken(TokenType.INTEGER, "123"));
    }

    @Test
    public void stringsAreTokenised() {
        assertTokens("'Tristan'", isToken(TokenType.STRING, "'Tristan'"));
    }

    @Test
    public void unterminatedStringsAreTokenised() {
        assertTokens("'Tristan", isToken(TokenType.UNTERMINATED_STRING, "'Tristan"));
    }

    @Test
    public void arrowsAreTokenised() {
        assertTokens("=>=>", isToken(TokenType.SYMBOL, "=>"), isToken(TokenType.SYMBOL, "=>"));
    }

    @Test
    public void classesAreTokenised() {
        assertTokens(".overture", isToken(TokenType.CLASS_NAME, ".overture"));
    }

    @Test
    public void colonsAreTokenised() {
        assertTokens("::", isToken(TokenType.SYMBOL, ":"), isToken(TokenType.SYMBOL, ":"));
    }

    @Test
    public void greaterThansAreTokenised() {
        assertTokens(">>", isToken(TokenType.SYMBOL, ">"), isToken(TokenType.SYMBOL, ">"));
    }

    @Test
    public void equalsAreTokenised() {
        assertTokens("==", isToken(TokenType.SYMBOL, "="), isToken(TokenType.SYMBOL, "="));
    }

    @Test
    public void openParensAreTokenised() {
        assertTokens("((", isToken(TokenType.SYMBOL, "("), isToken(TokenType.SYMBOL, "("));
    }

    @Test
    public void closeParensAreTokenised() {
        assertTokens("))", isToken(TokenType.SYMBOL, ")"), isToken(TokenType.SYMBOL, ")"));
    }

    @Test
    public void openSquareBracketsAreTokenised() {
        assertTokens("[[", isToken(TokenType.SYMBOL, "["), isToken(TokenType.SYMBOL, "["));
    }

    @Test
    public void closeSquareBracketsAreTokenised() {
        assertTokens("]]", isToken(TokenType.SYMBOL, "]"), isToken(TokenType.SYMBOL, "]"));
    }

    @Test
    public void choicesAreTokenised() {
        assertTokens("||", isToken(TokenType.SYMBOL, "|"), isToken(TokenType.SYMBOL, "|"));
    }

    @Test
    public void canTokeniseMultipleTokens() {
        assertTokens("The Magic Position",
            isToken(TokenType.IDENTIFIER, "The"),
            isToken(TokenType.WHITESPACE, " "),
            isToken(TokenType.IDENTIFIER, "Magic"),
            isToken(TokenType.WHITESPACE, " "),
            isToken(TokenType.IDENTIFIER, "Position")
        );
    }

    @SafeVarargs
    private final void assertTokens(String input, Matcher<Token>... token) {
        List<Matcher<? super Token>> tokens = eagerConcat(asList(token), list(isToken(TokenType.EOF, "")));
        assertThat(StyleMappingTokeniser.tokenise(input), contains(tokens));
    }

    private Matcher<Token> isToken(TokenType tokenType, String value) {
        return allOf(
            hasProperty("tokenType", equalTo(tokenType)),
            hasProperty("value", equalTo(value))
        );
    }
}
