package org.zwobble.mammoth;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlNodeVisitor;
import org.zwobble.mammoth.xml.parsing.XmlParser;
import org.zwobble.mammoth.xml.XmlTextNode;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableBiMap;

import static com.google.common.collect.Iterables.transform;

public class Mammoth {

    public static String convertToHtml(File file) {
        try (ZipFile zipFile = new ZipFile(file)) {
            ZipEntry entry = zipFile.getEntry("word/document.xml");
            XmlElement documentXml = parseXml(zipFile.getInputStream(entry));
            String text = extractText(documentXml);
            if (Strings.isNullOrEmpty(text)) {
                return "";
            } else {
                return "<p>" + text + "</p>";                
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException("Should return a result of failure");   
        }
    }

    private static XmlElement parseXml(InputStream inputStream) {
        return new XmlParser(ImmutableBiMap.of()).parseStream(inputStream);
    }

    private static String extractText(XmlElement documentXml) {
        return documentXml.accept(new XmlNodeVisitor<String>() {
            @Override
            public String visit(XmlElement element) {
                return Joiner.on("").join(
                    transform(element.children(), child -> child.accept(this)));
            }

            @Override
            public String visit(XmlTextNode textNode) {
                return textNode.getValue();
            }
        });
    }

}
