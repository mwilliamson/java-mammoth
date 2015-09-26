package org.zwobble.mammoth.tests;

import java.io.File;
import java.net.URISyntaxException;

import lombok.val;

public class TestData {
    public static File file(String name) {
        try {
            val uri = TestData.class.getResource("/test-data/" + name).toURI();
            return new File(uri);
        } catch (URISyntaxException exception) {
            throw new RuntimeException(exception);
        }
    }
}
