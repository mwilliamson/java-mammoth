package org.zwobble.mammoth.tests;

import java.io.IOException;
import java.io.InputStream;

public class TestData {
    public static InputStream stream(String name) {
        try {
            return TestData.class.getResource(name).openStream();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
