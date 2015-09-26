package org.zwobble.mammoth.tests;

import org.junit.Test;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class MammothTests {
    @Test
    public void canReadTestData() {
        assertThat(TestData.stream("/test-data/empty.docx"), notNullValue());
    }
}
