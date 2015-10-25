package org.zwobble.mammoth.tests;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class TestData {
    public static File file(String name) {
        try {
            URI uri = TestData.class.getResource("/test-data/" + name).toURI();
            return new File(uri);
        } catch (URISyntaxException exception) {
            throw new RuntimeException(exception);
        }
    }
}
