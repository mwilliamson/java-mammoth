package org.zwobble.mammoth.tests;

import java.io.File;

import org.junit.Test;
import org.zwobble.mammoth.Mammoth;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MammothTests {
    @Test
    public void emptyParagraphsAreIgnoredByDefault() {
        assertThat(convertToHtml("empty.docx"), is(""));
    }
    
    @Test
    public void docxContainingOneParagraphIsConvertedToSingleParagraphElement() {
        assertThat(
            convertToHtml("single-paragraph.docx"),
            is("<p>Walking on imported air</p>"));
    }

    private String convertToHtml(String name) {
        File file = TestData.file(name);
        return Mammoth.convertToHtml(file);
    }
}
