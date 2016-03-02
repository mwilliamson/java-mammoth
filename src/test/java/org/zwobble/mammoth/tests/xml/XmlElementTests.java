package org.zwobble.mammoth.tests.xml;

import org.junit.Test;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlNodes;

import static org.junit.Assert.assertEquals;
import static org.zwobble.mammoth.tests.util.MammothAsserts.assertThrows;

public class XmlElementTests {
    @Test
    public void TryingToGetNonExistentAttributeThrowsException() {
        XmlElement element = XmlNodes.element("p");

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> element.getAttribute("class"));

        assertEquals("Element has no 'class' attribute", exception.getMessage());
    }
}
