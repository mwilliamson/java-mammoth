package org.zwobble.mammoth.tests.docx;

import com.google.common.base.Charsets;
import org.junit.Test;
import org.zwobble.mammoth.internal.docx.OfficeXml;
import org.zwobble.mammoth.internal.xml.XmlElement;

import java.io.ByteArrayInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.internal.util.MammothLists.list;
import static org.zwobble.mammoth.internal.xml.XmlNodes.element;

public class OfficeXmlTests {
    @Test
    public void alternateContentIsReplacedByContentsOfFallback() {
        String xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<numbering xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">" +
            "<mc:AlternateContent>" +
            "<mc:Choice Requires=\"w14\">" +
            "<choice/>" +
            "</mc:Choice>" +
            "<mc:Fallback>" +
            "<fallback/>" +
            "</mc:Fallback>" +
            "</mc:AlternateContent>" +
            "</numbering>";

        XmlElement result = OfficeXml.parseXml(new ByteArrayInputStream(xmlString.getBytes(Charsets.UTF_8)));
        assertThat(result.children(), deepEquals(list(element("fallback"))));
    }
}
