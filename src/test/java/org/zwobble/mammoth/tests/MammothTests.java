package org.zwobble.mammoth.tests;

import org.junit.Test;
import org.zwobble.mammoth.Mammoth;
import org.zwobble.mammoth.results.Result;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zwobble.mammoth.results.Result.success;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;

public class MammothTests {
    @Test
    public void emptyParagraphsAreIgnoredByDefault() {
        assertThat(
            convertToHtml("empty.docx").getValue(),
            is(""));
    }
    
    @Test
    public void docxContainingOneParagraphIsConvertedToSingleParagraphElement() {
        assertThat(
            convertToHtml("single-paragraph.docx"),
            deepEquals(success("<p>Walking on imported air</p>")));
    }

    @Test
    public void canReadFilesWithUtf8Bom() {
        assertThat(
            convertToHtml("utf8-bom.docx"),
            deepEquals(success("<p>This XML has a byte order mark.</p>")));
    }

    private Result<String> convertToHtml(String name) {
        File file = TestData.file(name);
        return Mammoth.convertToHtml(file);
    }
}
