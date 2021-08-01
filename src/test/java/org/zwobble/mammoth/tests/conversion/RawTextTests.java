package org.zwobble.mammoth.tests.conversion;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.conversion.RawText;
import org.zwobble.mammoth.internal.documents.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.*;

public class RawTextTests {
    @Test
    public void textElementIsConvertedToTextContent() {
        DocumentElement element = new Text("Hello.");

        String result = RawText.extractRawText(element);

        assertThat(result, equalTo("Hello."));
    }

    @Test
    public void tabElementIsConvertedToTabCharacter() {
        DocumentElement element = Tab.TAB;

        String result = RawText.extractRawText(element);

        assertThat(result, equalTo("\t"));
    }

    @Test
    public void paragraphsAreTerminatedWithNewlines() {
        DocumentElement element = paragraph(
            withChildren(new Text("Hello "), new Text("world."))
        );

        String result = RawText.extractRawText(element);

        assertThat(result, equalTo("Hello world.\n\n"));
    }

    @Test
    public void childrenAreRecursivelyConvertedToText() {
        Document element = document(
            withChildren(
                paragraph(
                    withChildren(new Text("Hello "), new Text("world."))
                )
            )
        );

        String result = RawText.extractRawText(element);

        assertThat(result, equalTo("Hello world.\n\n"));
    }

    @Test
    public void nonTextElementWithoutChildrenIsConvertedToEmptyString() {
        DocumentElement element = Break.LINE_BREAK;

        String result = RawText.extractRawText(element);

        assertThat(result, equalTo(""));
    }
}
