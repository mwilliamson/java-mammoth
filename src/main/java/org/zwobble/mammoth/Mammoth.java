package org.zwobble.mammoth;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlNodeVisitor;
import org.zwobble.mammoth.xml.XmlParser;
import org.zwobble.mammoth.xml.XmlTextNode;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableBiMap;

import static com.google.common.collect.Iterables.transform;

import lombok.val;

public class Mammoth {

    public static String convertToHtml(File file) {
        try (val zipFile = new ZipFile(file)) {
            val entry = zipFile.getEntry("word/document.xml");
            val documentXml = parseXml(zipFile.getInputStream(entry));
            val text = extractText(documentXml);
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
            public String Visit(XmlTextNode textNode) {
                return textNode.getValue();
            }
        });
    }

}
