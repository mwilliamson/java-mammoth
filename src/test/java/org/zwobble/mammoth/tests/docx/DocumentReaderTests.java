package org.zwobble.mammoth.tests.docx;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.archives.Archive;
import org.zwobble.mammoth.internal.archives.InMemoryArchive;
import org.zwobble.mammoth.internal.documents.Document;
import org.zwobble.mammoth.internal.docx.DocumentReader;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.util.PassThroughException;
import org.zwobble.mammoth.internal.xml.NamespacePrefixes;
import org.zwobble.mammoth.internal.xml.XmlWriter;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.internal.xml.XmlNodes.element;
import static org.zwobble.mammoth.internal.xml.XmlNodes.text;
import static org.zwobble.mammoth.tests.ResultMatchers.isInternalSuccess;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.*;
import static org.zwobble.mammoth.tests.util.MammothAsserts.assertThrows;

public class DocumentReaderTests {
    private final NamespacePrefixes mainDocumentNamespaces = NamespacePrefixes.builder()
        .put("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        .build();
    private final NamespacePrefixes relationshipsNamespaces = NamespacePrefixes.builder()
        .defaultPrefix("http://schemas.openxmlformats.org/package/2006/relationships")
        .build();

    @Test
    public void mainDocumentIsFoundUsingPackageRelationships() {
        InternalResult<Document> result = DocumentReader.readDocument(Optional.empty(), InMemoryArchive.fromStrings(map(
            "word/document2.xml", XmlWriter.toString(
                element("w:document", list(
                    element("w:body", list(
                        element("w:p", list(
                            element("w:r", list(
                                element("w:t", list(text("Hello.")))
                            ))
                        ))
                    ))
                )),
                mainDocumentNamespaces
            ),
            "_rels/.rels", XmlWriter.toString(
                element("Relationships", list(
                    element("Relationship", map(
                        "Id", "rId1",
                        "Target", "/word/document2.xml",
                        "Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
                    ))
                )),
                relationshipsNamespaces
            )
        )));

        assertThat(result, isInternalSuccess(document(
            withChildren(paragraphWithText("Hello."))
        )));
    }

    @Test
    public void errorIsThrownWhenMainDocumentPartDoesNotExist() {
        Archive archive = InMemoryArchive.fromStrings(map(
            "_rels/.rels", XmlWriter.toString(
                element("Relationships", list(
                    element("Relationship", map(
                        "Id", "rId1",
                        "Target", "/word/document2.xml",
                        "Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
                    ))
                )),
                relationshipsNamespaces
            )
        ));
        PassThroughException exception = assertThrows(
            PassThroughException.class,
            () -> DocumentReader.readDocument(Optional.empty(), archive)
        );
        assertThat(exception.getMessage(), equalTo("java.io.IOException: Could not find main document part. Are you sure this is a valid .docx file?"));
    }
}
