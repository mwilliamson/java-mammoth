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
        assertTokens("=>=>", isToken(TokenType.ARROW, "=>"), isToken(TokenType.ARROW, "=>"));
    }

    @Test
    public void dotsAreTokenised() {
        assertTokens("..", isToken(TokenType.DOT, "."), isToken(TokenType.DOT, "."));
    }

    @Test
    public void colonsAreTokenised() {
        assertTokens("::", isToken(TokenType.COLON, ":"), isToken(TokenType.COLON, ":"));
    }

    @Test
    public void greaterThansAreTokenised() {
        assertTokens(">>", isToken(TokenType.GREATER_THAN, ">"), isToken(TokenType.GREATER_THAN, ">"));
    }

    @Test
    public void equalsAreTokenised() {
        assertTokens("==", isToken(TokenType.EQUALS, "="), isToken(TokenType.EQUALS, "="));
    }

    @Test
    public void openParensAreTokenised() {
        assertTokens("((", isToken(TokenType.OPEN_PAREN, "("), isToken(TokenType.OPEN_PAREN, "("));
    }

    @Test
    public void closeParensAreTokenised() {
        assertTokens("))", isToken(TokenType.CLOSE_PAREN, ")"), isToken(TokenType.CLOSE_PAREN, ")"));
    }

    @Test
    public void openSquareBracketsAreTokenised() {
        assertTokens("[[", isToken(TokenType.OPEN_SQUARE_BRACKET, "["), isToken(TokenType.OPEN_SQUARE_BRACKET, "["));
    }

    @Test
    public void closeSquareBracketsAreTokenised() {
        assertTokens("]]", isToken(TokenType.CLOSE_SQUARE_BRACKET, "]"), isToken(TokenType.CLOSE_SQUARE_BRACKET, "]"));
    }

    @Test
    public void choicesAreTokenised() {
        assertTokens("||", isToken(TokenType.CHOICE, "|"), isToken(TokenType.CHOICE, "|"));
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
