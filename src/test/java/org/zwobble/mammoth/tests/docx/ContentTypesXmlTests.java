package org.zwobble.mammoth.tests.docx;

import org.junit.Test;
import org.zwobble.mammoth.internal.docx.ContentTypes;
import org.zwobble.mammoth.internal.xml.XmlElement;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.zwobble.mammoth.internal.docx.ContentTypesXml.readContentTypesXmlElement;
import static org.zwobble.mammoth.internal.util.MammothLists.list;
import static org.zwobble.mammoth.internal.util.MammothMaps.map;
import static org.zwobble.mammoth.internal.xml.XmlNodes.element;

public class ContentTypesXmlTests {
    @Test
    public void contentTypeIsBasedOnDefaultForExtensionIfThereIsNoOverride() {
        XmlElement element = element("content-types:Types", list(
            element("content-types:Default", map(
                "Extension", "png",
                "ContentType", "image/png"))));
        ContentTypes contentTypes = readContentTypesXmlElement(element);
        assertEquals(
            Optional.of("image/png"),
            contentTypes.findContentType("word/media/hat.png"));
    }

    @Test
    public void contentTypeIsBasedOnOverrideIfPresent() {
        XmlElement element = element("content-types:Types", list(
            element("content-types:Default", map(
                "Extension", "png",
                "ContentType", "image/png")),
            element("content-types:Override", map(
                "PartName", "/word/media/hat.png",
                "ContentType", "image/hat"))));
        ContentTypes contentTypes = readContentTypesXmlElement(element);
        assertEquals(
            Optional.of("image/hat"),
            contentTypes.findContentType("word/media/hat.png"));
    }

    @Test
    public void fallbackContentTypesHaveCommonImageTypes() {
        XmlElement element = element("content-types:Types");
        ContentTypes contentTypes = readContentTypesXmlElement(element);
        assertEquals(
            Optional.of("image/png"),
            contentTypes.findContentType("word/media/hat.png"));
        assertEquals(
            Optional.of("image/gif"),
            contentTypes.findContentType("word/media/hat.gif"));
        assertEquals(
            Optional.of("image/jpeg"),
            contentTypes.findContentType("word/media/hat.jpg"));
        assertEquals(
            Optional.of("image/jpeg"),
            contentTypes.findContentType("word/media/hat.jpeg"));
        assertEquals(
            Optional.of("image/bmp"),
            contentTypes.findContentType("word/media/hat.bmp"));
        assertEquals(
            Optional.of("image/tiff"),
            contentTypes.findContentType("word/media/hat.tif"));
        assertEquals(
            Optional.of("image/tiff"),
            contentTypes.findContentType("word/media/hat.tiff"));
    }

    @Test
    public void fallbackContentTypesAreCaseInsensitive() {
        XmlElement element = element("content-types:Types");
        ContentTypes contentTypes = readContentTypesXmlElement(element);
        assertEquals(
            Optional.of("image/png"),
            contentTypes.findContentType("word/media/hat.PnG"));
    }
}
