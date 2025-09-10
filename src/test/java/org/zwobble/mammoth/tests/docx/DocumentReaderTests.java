package org.zwobble.mammoth.tests.docx;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.zwobble.mammoth.internal.archives.Archive;
import org.zwobble.mammoth.internal.archives.InMemoryArchive;
import org.zwobble.mammoth.internal.documents.Document;
import org.zwobble.mammoth.internal.docx.DocumentReader;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.util.PassThroughException;
import org.zwobble.mammoth.internal.xml.NamespacePrefixes;
import org.zwobble.mammoth.internal.xml.XmlWriter;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.zwobble.mammoth.internal.util.Lists.eagerFlatMap;
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
        Archive archive = InMemoryArchive.fromStrings(map(
            "word/document2.xml", XmlWriter.toString(
                element(
                    "w:document", list(
                        element(
                            "w:body", list(
                                element(
                                    "w:p", list(
                                        element(
                                            "w:r", list(
                                                element("w:t", list(text("Hello.")))
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                mainDocumentNamespaces
            ),
            "_rels/.rels", XmlWriter.toString(
                element(
                    "Relationships", list(
                        element(
                            "Relationship", map(
                                "Id", "rId1",
                                "Target", "/word/document2.xml",
                                "Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
                            )
                        )
                    )
                ),
                relationshipsNamespaces
            )
        ));
        InternalResult<Document> result = DocumentReader.readDocument(
            Optional.empty(),
            archive,
            false
        );

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
            () -> DocumentReader.readDocument(Optional.empty(), archive, false)
        );
        assertThat(exception.getMessage(), equalTo("java.io.IOException: Could not find main document part. Are you sure this is a valid .docx file?"));
    }

    @Nested
    public class PartPathTests {
        @Test
        public void mainDocumentPartIsFoundUsingPackageRelationships() {
            InMemoryArchive archive = InMemoryArchive.fromStrings(map(
                "word/document2.xml", " ",
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

            DocumentReader.PartPaths partPaths = DocumentReader.findPartPaths(archive);
            assertThat(partPaths.getMainDocument(), equalTo("word/document2.xml"));
        }

        @Test
        public void whenRelationshipForMainDocumentCannotBeFoundThenFallbackIsUsed() {
            InMemoryArchive archive = InMemoryArchive.fromStrings(map(
                "word/document.xml", " "
            ));

            DocumentReader.PartPaths partPaths = DocumentReader.findPartPaths(archive);
            assertThat(partPaths.getMainDocument(), equalTo("word/document.xml"));
        }

        @TestFactory
        public List<DynamicTest> partsRelatedToMainDocument() {
            return eagerFlatMap(
                list("comments", "endnotes", "footnotes", "numbering", "styles"),
                name -> list(
                    DynamicTest.dynamicTest(name + " part is found using main document relationships", () -> {
                        partIsFoundUsingMainDocumentRelationships(name);
                    }),
                    DynamicTest.dynamicTest(name + " part is found using main document relationships", () -> {
                        whenRelationshipForPartCannotBeFoundThenFallbackIsUsed(name);
                    })
                )
            );
        }

        private void partIsFoundUsingMainDocumentRelationships(String name) {
            InMemoryArchive archive = InMemoryArchive.fromStrings(map(
                "_rels/.rels", XmlWriter.toString(
                    element("Relationships", list(
                        element("Relationship", map(
                            "Id", "rId1",
                            "Target", "/word/document.xml",
                            "Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
                        ))
                    )),
                    relationshipsNamespaces
                ),
                "word/document.xml", " ",
                "word/_rels/document.xml.rels", XmlWriter.toString(
                    element("Relationships", list(
                        element("Relationship", map(
                            "Id", "rId2",
                            "Target", "target-path.xml",
                            "Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/" + name
                        ))
                    )),
                    relationshipsNamespaces
                ),
                "word/target-path.xml", " "
            ));

            DocumentReader.PartPaths partPaths = DocumentReader.findPartPaths(archive);
            assertThat(partPaths, hasProperty(name, equalTo("word/target-path.xml")));
        }

        private void whenRelationshipForPartCannotBeFoundThenFallbackIsUsed(String name) {
            InMemoryArchive archive = InMemoryArchive.fromStrings(map(
                "word/document.xml", " ",
                "_rels/.rels", XmlWriter.toString(
                    element("Relationships", list(
                        element("Relationship", map(
                            "Id", "rId1",
                            "Target", "/word/document.xml",
                            "Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
                        ))
                    )),
                    relationshipsNamespaces
                )
            ));

            DocumentReader.PartPaths partPaths = DocumentReader.findPartPaths(archive);
            assertThat(partPaths, hasProperty(name, equalTo("word/" + name + ".xml")));
        }
    }
}
