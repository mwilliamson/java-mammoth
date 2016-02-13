package org.zwobble.mammoth.tests;

import org.junit.Test;
import org.zwobble.mammoth.Mammoth;
import org.zwobble.mammoth.Result;

import java.io.File;

import static org.junit.Assert.assertThat;
import static org.zwobble.mammoth.Result.success;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;

public class MammothTests {
    @Test
    public void emptyParagraphsAreIgnoredByDefault() {
        assertThat(
            convertToHtml("empty.docx"),
            deepEquals(success("")));
    }
    
    @Test
    public void docxContainingOneParagraphIsConvertedToSingleParagraphElement() {
        assertThat(
            convertToHtml("single-paragraph.docx"),
            deepEquals(success("<p>Walking on imported air</p>")));
    }

    private Result<String> convertToHtml(String name) {
        File file = TestData.file(name);
        return Mammoth.convertToHtml(file);
    }
}
