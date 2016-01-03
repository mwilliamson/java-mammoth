package org.zwobble.mammoth;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import org.zwobble.mammoth.documents.*;
import org.zwobble.mammoth.docx.BodyXmlReader;
import org.zwobble.mammoth.docx.DocumentXmlReader;
import org.zwobble.mammoth.docx.Styles;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.parsing.XmlParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.google.common.collect.Iterables.transform;

public class Mammoth {
    private static ImmutableBiMap<String, String> XML_NAMESPACES = ImmutableBiMap.<String, String>builder()
        .put("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        .put("wp", "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing")
        .put("a", "http://schemas.openxmlformats.org/drawingml/2006/main")
        .put("pic", "http://schemas.openxmlformats.org/drawingml/2006/picture")
        .put("content-types", "http://schemas.openxmlformats.org/package/2006/content-types")
        .put("r", "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
        .put("relationships", "http://schemas.openxmlformats.org/package/2006/relationships")
        .put("v", "urn:schemas-microsoft-com:vml")
        .put("mc", "http://schemas.openxmlformats.org/markup-compatibility/2006")
        .put("office-word", "urn:schemas-microsoft-com:office:word")
        .build();

    public static String convertToHtml(File file) {
        try (ZipFile zipFile = new ZipFile(file)) {
            ZipEntry entry = zipFile.getEntry("word/document.xml");
            XmlElement documentXml = parseXml(zipFile.getInputStream(entry));

            Styles styles = new Styles(ImmutableMap.of(), ImmutableMap.of());
            Document document = new DocumentXmlReader(new BodyXmlReader(styles)).readElement(documentXml);
            return convertToHtml(document);
        } catch (IOException e) {
            throw new UnsupportedOperationException("Should return a result of failure");   
        }
    }

    private static String convertToHtml(Document document) {
        return convertChildrenToHtml(document);
    }

    private static String convertChildrenToHtml(HasChildren element) {
        return Joiner.on("").join(transform(
            element.getChildren(),
            Mammoth::convertToHtml));
    }

    private static String convertToHtml(DocumentElement element) {
        return element.accept(new DocumentElementVisitor<String>() {
            @Override
            public String visit(Paragraph paragraph) {
                String content = convertChildrenToHtml(paragraph);
                if (content.isEmpty()) {
                    return "";
                } else {
                    return "<p>" + content + "</p>";
                }
            }

            @Override
            public String visit(Run run) {
                return convertChildrenToHtml(run);
            }

            @Override
            public String visit(Text text) {
                return text.getValue();
            }
        });
    }

    private static XmlElement parseXml(InputStream inputStream) {
        return new XmlParser(XML_NAMESPACES).parseStream(inputStream);
    }
}
