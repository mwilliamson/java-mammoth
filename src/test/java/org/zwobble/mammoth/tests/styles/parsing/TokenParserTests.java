package org.zwobble.mammoth.tests.styles.parsing;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.styles.parsing.Token;
import org.zwobble.mammoth.internal.styles.parsing.TokenIterator;
import org.zwobble.mammoth.internal.styles.parsing.TokenParser;
import org.zwobble.mammoth.internal.styles.parsing.TokenType;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.zwobble.mammoth.internal.util.Lists.list;

public class TokenParserTests {
    @Test
    public void escapeSequencesInIdentifiersAreDecoded() {
        Optional<String> value = TokenParser.parseClassName(new TokenIterator<>(list(
            new Token<>(0, TokenType.SYMBOL, "."),
            new Token<>(1, TokenType.IDENTIFIER, "\\:")
        ), new Token<>(2, TokenType.EOF, "")));
        assertEquals(Optional.of(":"), value);
    }

    @Test
    public void escapeSequencesInStringAreDecoded() {
        String value = TokenParser.parseString(new TokenIterator<>(list(
            new Token<>(0, TokenType.STRING, "'\\n'")
        ), new Token<>(1, TokenType.EOF, "")));
        assertEquals("\n", value);
    }
}
