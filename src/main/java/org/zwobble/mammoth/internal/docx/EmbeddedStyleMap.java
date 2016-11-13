package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.archives.Archive;
import org.zwobble.mammoth.internal.archives.Archives;
import org.zwobble.mammoth.internal.archives.MutableArchive;
import org.zwobble.mammoth.internal.util.Streams;
import org.zwobble.mammoth.internal.xml.*;
import org.zwobble.mammoth.internal.xml.parsing.XmlParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.Casts.tryCast;
import static org.zwobble.mammoth.internal.util.Iterables.findIndex;
import static org.zwobble.mammoth.internal.util.Maps.map;

public class EmbeddedStyleMap {
    private static final String STYLE_MAP_PATH = "mammoth/style-map";
    private static final String ABSOLUTE_STYLE_MAP_PATH = "/" + STYLE_MAP_PATH;
    private static final String RELATIONSHIPS_PATH = "word/_rels/document.xml.rels";
    private static final String CONTENT_TYPES_PATH = "[Content_Types].xml";

    public static final NamespacePrefixes RELATIONSHIPS_NAMESPACES = NamespacePrefixes.builder()
        .defaultPrefix("http://schemas.openxmlformats.org/package/2006/relationships")
        .build();

    public static final NamespacePrefixes CONTENT_TYPES_NAMESPACES = NamespacePrefixes.builder()
        .defaultPrefix("http://schemas.openxmlformats.org/package/2006/content-types")
        .build();

    public static Optional<String> readStyleMap(Archive file) throws IOException {
        return file.tryGetInputStream(STYLE_MAP_PATH).map(Streams::toString);
    }

    public static void embedStyleMap(MutableArchive archive, String styleMap) throws IOException {
        archive.writeEntry(STYLE_MAP_PATH, styleMap);
        updateRelationships(archive);
        updateContentTypes(archive);
    }

    private static void updateRelationships(MutableArchive archive) throws IOException {
        XmlParser parser = new XmlParser(RELATIONSHIPS_NAMESPACES);
        XmlElement relationships = parser.parseStream(Archives.getInputStream(archive, RELATIONSHIPS_PATH));
        XmlElement relationship = XmlNodes.element("Relationship", map(
            "Id", "rMammothStyleMap",
            "Type", "http://schemas.zwobble.org/mammoth/style-map",
            "Target", ABSOLUTE_STYLE_MAP_PATH
        ));
        XmlElement updatedRelationships = updateOrAddElement(relationships, relationship, "Id");
        archive.writeEntry(RELATIONSHIPS_PATH, XmlWriter.toString(updatedRelationships, RELATIONSHIPS_NAMESPACES));
    }

    private static void updateContentTypes(MutableArchive archive) throws IOException {
        XmlParser parser = new XmlParser(CONTENT_TYPES_NAMESPACES);
        XmlElement contentTypes = parser.parseStream(Archives.getInputStream(archive, CONTENT_TYPES_PATH));
        XmlElement override = XmlNodes.element("Override", map(
            "PartName", ABSOLUTE_STYLE_MAP_PATH,
            "ContentType", "text/prs.mammoth.style-map"
        ));
        XmlElement updatedRelationships = updateOrAddElement(contentTypes, override, "PartName");
        archive.writeEntry(CONTENT_TYPES_PATH, XmlWriter.toString(updatedRelationships, CONTENT_TYPES_NAMESPACES));
    }

    private static XmlElement updateOrAddElement(XmlElement parent, XmlElement element, String identifyingAttribute) {
        int index = findIndex(parent.getChildren(), child ->
            tryCast(XmlElement.class, child)
                .map(childElement ->
                    childElement.getName().equals(element.getName()) &&
                    childElement.getAttributeOrNone(identifyingAttribute).equals(element.getAttributeOrNone(identifyingAttribute))
                )
                .orElse(false)
        );
        List<XmlNode> children = new ArrayList<>(parent.getChildren());

        if (index == -1) {
            children.add(element);
        } else {
            children.set(index, element);
        }
        return new XmlElement(parent.getName(), parent.getAttributes(), children);
    }
}
