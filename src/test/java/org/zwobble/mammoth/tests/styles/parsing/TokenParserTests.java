package org.zwobble.mammoth.tests.styles.parsing;

import org.junit.Test;
import org.zwobble.mammoth.internal.styles.parsing.Token;
import org.zwobble.mammoth.internal.styles.parsing.TokenIterator;
import org.zwobble.mammoth.internal.styles.parsing.TokenParser;
import org.zwobble.mammoth.internal.styles.parsing.TokenType;

import static org.junit.Assert.assertEquals;
import static org.zwobble.mammoth.internal.util.Lists.list;

public class TokenParserTests {
    @Test
    public void escapeSequencesInClassNameAreDecoded() {
        String value = TokenParser.parseClassName(new TokenIterator<>(list(
            new Token<>(0, TokenType.CLASS_NAME, "\\:")
        )));
        assertEquals(":", value);
    }

    @Test
    public void escapeSequencesInStringAreDecoded() {
        String value = TokenParser.parseString(new TokenIterator<>(list(
            new Token<>(0, TokenType.STRING, "'\\n'")
        )));
        assertEquals("\n", value);
    }
}
