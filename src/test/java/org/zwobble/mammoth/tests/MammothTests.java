package org.zwobble.mammoth.tests;

import org.junit.Test;
import org.zwobble.mammoth.Mammoth;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import lombok.val;

public class MammothTests {
    @Test
    public void emptyParagraphsAreIgnoredByDefault() {
        assertThat(convertToHtml("/test-data/empty.docx"), is(""));
    }

    private String convertToHtml(String name) {
        val stream = TestData.stream(name);
        return Mammoth.convertToHtml(stream);
    }
}
