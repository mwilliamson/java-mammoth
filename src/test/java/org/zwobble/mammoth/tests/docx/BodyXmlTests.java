package org.zwobble.mammoth.tests.docx;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.zwobble.mammoth.internal.archives.Archive;
import org.zwobble.mammoth.internal.archives.InMemoryArchive;
import org.zwobble.mammoth.internal.documents.*;
import org.zwobble.mammoth.internal.docx.*;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlNode;
import org.zwobble.mammoth.internal.xml.XmlNodes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.zwobble.mammoth.internal.documents.NoteReference.endnoteReference;
import static org.zwobble.mammoth.internal.documents.NoteReference.footnoteReference;
import static org.zwobble.mammoth.internal.util.Lists.eagerMap;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.internal.util.Streams.toByteArray;
import static org.zwobble.mammoth.internal.xml.XmlNodes.element;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.tests.ResultMatchers.*;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.*;
import static org.zwobble.mammoth.tests.docx.BodyXmlReaderMakers.bodyReader;
import static org.zwobble.mammoth.tests.docx.DocumentMatchers.*;
import static org.zwobble.mammoth.tests.docx.OfficeXmlBuilders.*;

public class BodyXmlTests {
    @Test
    public void textFromTextElementIsRead() {
        XmlElement element = textXml("Hello!");
        assertThat(readSuccess(bodyReader(), element), isTextElement("Hello!"));
    }

    @Test
    public void canReadTextWithinRun() {
        XmlElement element = runXml(list(textXml("Hello!")));
        assertThat(
            readSuccess(bodyReader(), element),
            isRun(run(withChildren(text("Hello!")))));
    }

    @Test
    public void canReadTextWithinParagraph() {
        XmlElement element = paragraphXml(list(runXml(list(textXml("Hello!")))));
        assertThat(
            readSuccess(bodyReader(), element),
            isParagraph(paragraph(withChildren(run(withChildren(text("Hello!")))))));
    }

    @Test
    public void paragraphHasNoStyleIfItHasNoProperties() {
        XmlElement element = paragraphXml();
        assertThat(
            readSuccess(bodyReader(), element),
            hasStyle(Optional.empty()));
    }

    @Test
    public void whenParagraphHasStyleIdInStylesThenStyleNameIsReadFromStyles() {
        XmlElement element = paragraphXml(list(
            element("w:pPr", list(
                element("w:pStyle", map("w:val", "Heading1"))))));

        Style style = new Style("Heading1", Optional.of("Heading 1"));
        Styles styles = new Styles(
            map("Heading1", style),
            map(),
            map(),
            map()
        );
        assertThat(
            readSuccess(bodyReader(styles), element),
            hasStyle(Optional.of(style)));
    }

    @Test
    public void warningIsEmittedWhenParagraphStyleCannotBeFound() {
        XmlElement element = paragraphXml(list(
            element("w:pPr", list(
                element("w:pStyle", map("w:val", "Heading1"))))));
        assertThat(
            read(bodyReader(), element),
            isInternalResult(
                hasStyle(Optional.of(new Style("Heading1", Optional.empty()))),
                list("Paragraph style with ID Heading1 was referenced but not defined in the document")));
    }

    @Nested
    public class ParagraphIndentTests {
        @Test
        public void whenWStartIsSetThenStartIndentIsReadFromWStart() {
            XmlElement paragraphXml = paragraphWithIndent(map("w:start", "720", "w:left", "40"));
            assertThat(
                readSuccess(bodyReader(), paragraphXml),
                hasIndent(hasIndentStart(Optional.of("720")))
            );
        }

        @Test
        public void whenWStartIsNotSetThenStartIndentIsReadFromWLeft() {
            XmlElement paragraphXml = paragraphWithIndent(map("w:left", "720"));
            assertThat(
                readSuccess(bodyReader(), paragraphXml),
                hasIndent(hasIndentStart(Optional.of("720")))
            );
        }

        @Test
        public void whenWEndIsSetThenEndIndentIsReadFromWEnd() {
            XmlElement paragraphXml = paragraphWithIndent(map("w:end", "720", "w:right", "40"));
            assertThat(
                readSuccess(bodyReader(), paragraphXml),
                hasIndent(hasIndentEnd(Optional.of("720")))
            );
        }

        @Test
        public void whenWEndIsNotSetThenEndIndentIsReadFromWRight() {
            XmlElement paragraphXml = paragraphWithIndent(map("w:right", "720"));
            assertThat(
                readSuccess(bodyReader(), paragraphXml),
                hasIndent(hasIndentEnd(Optional.of("720")))
            );
        }

        @Test
        public void paragraphHasIndentFirstLineReadFromParagraphPropertiesIfPresent() {
            XmlElement paragraphXml = paragraphWithIndent(map("w:firstLine", "720"));
            assertThat(
                readSuccess(bodyReader(), paragraphXml),
                hasIndent(hasIndentFirstLine(Optional.of("720")))
            );
        }

        @Test
        public void paragraphHasIndentHangingReadFromParagraphPropertiesIfPresent() {
            XmlElement paragraphXml = paragraphWithIndent(map("w:hanging", "720"));
            assertThat(
                readSuccess(bodyReader(), paragraphXml),
                hasIndent(hasIndentHanging(Optional.of("720")))
            );
        }

        @Test
        public void whenIndentAttributesArentSetThenIndentsAreNotSet() {
            XmlElement paragraphXml = paragraphWithIndent(map());
            assertThat(
                readSuccess(bodyReader(), paragraphXml),
                hasIndent(allOf(
                    hasIndentStart(Optional.empty()),
                    hasIndentEnd(Optional.empty()),
                    hasIndentFirstLine(Optional.empty()),
                    hasIndentHanging(Optional.empty())
                ))
            );
        }

        private XmlElement paragraphWithIndent(Map<String, String> attributes) {
            return paragraphXml(list(
                element("w:pPr", list(
                    element("w:ind", attributes)
                ))
            ));
        }
    }

    @Test
    public void paragraphHasNoNumberingIfItHasNoNumberingProperties() {
        XmlElement element = paragraphXml();
        assertThat(
            readSuccess(bodyReader(), element),
            hasNumbering(Optional.empty()));
    }

    @Test
    public void paragraphHasNumberingPropertiesFromParagraphPropertiesIfPresent() {
        XmlElement element = paragraphXml(list(
            element("w:pPr", list(
                element("w:numPr", map(), list(
                    element("w:ilvl", map("w:val", "1")),
                    element("w:numId", map("w:val", "42"))))))));

        Numbering numbering = numberingMap(map("42", map("1", Numbering.AbstractNumLevel.ordered("1"))));

        assertThat(
            readSuccess(bodyReader(numbering), element),
            hasNumbering(NumberingLevel.ordered("1")));
    }

    @Test
    public void paragraphHasNumberingFromParagraphStyleIfPresent() {
        XmlElement element = paragraphXml(list(
            element("w:pPr", list(
                element("w:pStyle", map("w:val", "List"))
            ))
        ));

        Numbering numbering = numberingMap(map(
            "43", map("1", new Numbering.AbstractNumLevel("1", true, Optional.of("List")))
        ));
        Styles styles = new Styles(
            map("List", new Style("List", Optional.empty())),
            map(),
            map(),
            map()
        );

        assertThat(
            readSuccess(bodyReader(numbering, styles), element),
            hasNumbering(NumberingLevel.ordered("1"))
        );
    }

    @Test
    public void numberingPropertiesInParagraphPropertiesTakesPrecedenceOverNumberingInParagraphStyle() {
        XmlElement element = paragraphXml(list(
            element("w:pPr", list(
                element("w:pStyle", map("w:val", "List")),
                element("w:numPr", map(), list(
                    element("w:ilvl", map("w:val", "1")),
                    element("w:numId", map("w:val", "42"))
                ))
            ))
        ));

        Numbering numbering = numberingMap(map(
            "42", map("1", new Numbering.AbstractNumLevel("1", true, Optional.empty())),
            "43", map("1", new Numbering.AbstractNumLevel("2", true, Optional.of("List")))
        ));
        Styles styles = new Styles(
            map("List", new Style("List", Optional.empty())),
            map(),
            map(),
            map()
        );

        assertThat(
            readSuccess(bodyReader(numbering, styles), element),
            hasNumbering(NumberingLevel.ordered("1"))
        );
    }

    @Test
    public void whenNumberingPropertiesAreMissingLevelThenLevelOf0IsAssumed() {
        // TODO: emit warning
        XmlElement element = paragraphXml(list(
            element("w:pPr", list(
                element("w:numPr", map(), list(
                    element("w:numId", map("w:val", "42"))
                ))
            ))
        ));

        Numbering numbering = numberingMap(map("42", map("0", Numbering.AbstractNumLevel.ordered("0"))));

        assertThat(
            readSuccess(bodyReader(numbering), element),
            hasNumbering(NumberingLevel.ordered("0")));
    }

    @Test
    public void numberingPropertiesAreIgnoredIfNumIdIsMissing() {
        // TODO: emit warning
        XmlElement element = paragraphXml(list(
            element("w:pPr", list(
                element("w:numPr", map(), list(
                    element("w:ilvl", map("w:val", "1"))))))));

        Numbering numbering = numberingMap(map("42", map("1", Numbering.AbstractNumLevel.ordered("1"))));

        assertThat(
            readSuccess(bodyReader(numbering), element),
            hasNumbering(Optional.empty()));
    }

    @Test
    public void contentOfDeletedParagraphIsPreprendedToNextParagraph() {
        Style heading1 = new Style("Heading1", Optional.of("Heading 1"));
        Style heading2 = new Style("Heading2", Optional.of("Heading 2"));
        Styles styles = new Styles(
            map(
                "Heading1", heading1,
                "Heading2", heading2
            ),
            map(),
            map(),
            map()
        );
        List<XmlNode> bodyXml = list(
            element("w:p", list(
                element("w:pPr", list(
                    element("w:pStyle", map("w:val", "Heading1")),
                    element("w:rPr", list(
                        element("w:del")
                    ))
                )),
                runXml("One")
            )),
            element("w:p", list(
                element("w:pPr", list(
                    element("w:pStyle", map("w:val", "Heading2"))
                )),
                runXml("Two")
            )),
            // Include a second paragraph that isn't deleted to ensure we only add
            // the deleted paragraph contents once.
            element("w:p", list(
                runXml("Three")
            ))
        );

        BodyXmlReader reader = bodyReader(styles);
        InternalResult<List<DocumentElement>> result = reader.readElements(bodyXml).toResult();

        assertThat(result.getWarnings(), emptyIterable());
        assertThat(result.getValue(), contains(
            isParagraph(
                hasParagraphStyle(equalTo(Optional.of(heading2))),
                hasChildren(
                    isRun(run(withChildren(text("One")))),
                    isRun(run(withChildren(text("Two"))))
                )
            ),
            isParagraph(
                hasChildren(
                    isRun(run(withChildren(text("Three"))))
                )
            )
        ));
    }

    @Nested
    public class ComplexFieldsTests {
        private final String URI = "http://example.com";
        private final XmlElement BEGIN_COMPLEX_FIELD = element("w:r", list(
            element("w:fldChar", map("w:fldCharType", "begin"))
        ));
        private final XmlElement SEPARATE_COMPLEX_FIELD = element("w:r", list(
            element("w:fldChar", map("w:fldCharType", "separate"))
        ));
        private final XmlElement END_COMPLEX_FIELD = element("w:r", list(
            element("w:fldChar", map("w:fldCharType", "end"))
        ));
        private final XmlElement HYPERLINK_INSTRTEXT = element("w:instrText", list(
            XmlNodes.text(" HYPERLINK \"" + URI + '"')
        ));
        private Matcher<DocumentElement> isEmptyHyperlinkedRun() {
            return isHyperlinkedRun(hasChildren());
        }

        @SafeVarargs
        private final Matcher<DocumentElement> isHyperlinkedRun(Matcher<? super Hyperlink>... matchers) {
            return isRun(hasChildren(
                isHyperlink(matchers)
            ));
        }

        @Test
        public void runsInAComplexFieldForHyperlinksWithoutSwitchAreReadAsExternalHyperlinks() {
            XmlElement hyperlinkRunXml = runXml("this is a hyperlink");
            XmlElement element = paragraphXml(list(
                BEGIN_COMPLEX_FIELD,
                HYPERLINK_INSTRTEXT,
                SEPARATE_COMPLEX_FIELD,
                hyperlinkRunXml,
                END_COMPLEX_FIELD
            ));
            DocumentElement paragraph = readSuccess(bodyReader(), element);

            assertThat(paragraph, isParagraph(hasChildren(
                isEmptyRun(),
                isEmptyHyperlinkedRun(),
                isHyperlinkedRun(
                    hasHref(URI),
                    hasChildren(
                        isTextElement("this is a hyperlink")
                    )
                ),
                isEmptyRun()
            )));
        }

        @Test
        public void runsInAComplexFieldForHyperlinksWithLSwitchAreReadAsInternalHyperlinks() {
            XmlElement hyperlinkRunXml = runXml("this is a hyperlink");
            XmlElement element = paragraphXml(list(
                BEGIN_COMPLEX_FIELD,
                element("w:instrText", list(
                    XmlNodes.text(" HYPERLINK \\l \"InternalLink\"")
                )),
                SEPARATE_COMPLEX_FIELD,
                hyperlinkRunXml,
                END_COMPLEX_FIELD
            ));
            DocumentElement paragraph = readSuccess(bodyReader(), element);

            assertThat(paragraph, isParagraph(hasChildren(
                isEmptyRun(),
                isEmptyHyperlinkedRun(),
                isHyperlinkedRun(
                    hasAnchor("InternalLink"),
                    hasChildren(
                        isTextElement("this is a hyperlink")
                    )
                ),
                isEmptyRun()
            )));
        }

        @Test
        public void runsAfterAComplexFieldForHyperlinksAreNotReadAsHyperlinks() {
            XmlElement afterEndXml = runXml("this will not be a hyperlink");
            XmlElement element = paragraphXml(list(
                BEGIN_COMPLEX_FIELD,
                HYPERLINK_INSTRTEXT,
                SEPARATE_COMPLEX_FIELD,
                END_COMPLEX_FIELD,
                afterEndXml
            ));
            DocumentElement paragraph = readSuccess(bodyReader(), element);

            assertThat(paragraph, isParagraph(hasChildren(
                isEmptyRun(),
                isEmptyHyperlinkedRun(),
                isEmptyRun(),
                isRun(hasChildren(
                    isTextElement("this will not be a hyperlink")
                ))
            )));
        }

        @Test
        public void canHandleSplitInstrTextElements() {
            XmlElement hyperlinkRunXml = runXml("this is a hyperlink");
            XmlElement element = paragraphXml(list(
                BEGIN_COMPLEX_FIELD,
                element("w:instrText", list(
                    XmlNodes.text(" HYPE")
                )),
                element("w:instrText", list(
                    XmlNodes.text("RLINK \"" + URI + '"')
                )),
                SEPARATE_COMPLEX_FIELD,
                hyperlinkRunXml,
                END_COMPLEX_FIELD
            ));
            DocumentElement paragraph = readSuccess(bodyReader(), element);

            assertThat(paragraph, isParagraph(hasChildren(
                isEmptyRun(),
                isEmptyHyperlinkedRun(),
                isHyperlinkedRun(
                    hasHref(URI),
                    hasChildren(
                        isTextElement("this is a hyperlink")
                    )
                ),
                isEmptyRun()
            )));
        }

        @Test
        public void hyperlinkIsNotEndedByEndOfNestedComplexField() {
            XmlElement authorInstrText = element("w:instrText", list(
                XmlNodes.text(" AUTHOR \"John Doe\"")
            ));
            XmlElement hyperlinkRunXml = runXml("this is a hyperlink");
            XmlElement element = paragraphXml(list(
                BEGIN_COMPLEX_FIELD,
                HYPERLINK_INSTRTEXT,
                SEPARATE_COMPLEX_FIELD,
                BEGIN_COMPLEX_FIELD,
                authorInstrText,
                SEPARATE_COMPLEX_FIELD,
                END_COMPLEX_FIELD,
                hyperlinkRunXml,
                END_COMPLEX_FIELD
            ));
            DocumentElement paragraph = readSuccess(bodyReader(), element);

            assertThat(paragraph, isParagraph(hasChildren(
                isEmptyRun(),
                isEmptyHyperlinkedRun(),
                isEmptyHyperlinkedRun(),
                isEmptyHyperlinkedRun(),
                isEmptyHyperlinkedRun(),
                isHyperlinkedRun(
                    hasHref(URI),
                    hasChildren(
                        isTextElement("this is a hyperlink")
                    )
                ),
                isEmptyRun()
            )));
        }

        @Test
        public void complexFieldNestedWithinAHyperlinkComplexFieldIsWrappedWithTheHyperlink() {
            XmlElement authorInstrText = element("w:instrText", list(
                XmlNodes.text(" AUTHOR \"John Doe\"")
            ));
            XmlElement element = paragraphXml(list(
                BEGIN_COMPLEX_FIELD,
                HYPERLINK_INSTRTEXT,
                SEPARATE_COMPLEX_FIELD,
                BEGIN_COMPLEX_FIELD,
                authorInstrText,
                SEPARATE_COMPLEX_FIELD,
                runXml("John Doe"),
                END_COMPLEX_FIELD,
                END_COMPLEX_FIELD
            ));
            DocumentElement paragraph = readSuccess(bodyReader(), element);

            assertThat(paragraph, isParagraph(hasChildren(
                isEmptyRun(),
                isEmptyHyperlinkedRun(),
                isEmptyHyperlinkedRun(),
                isEmptyHyperlinkedRun(),
                isHyperlinkedRun(
                    hasHref(URI),
                    hasChildren(
                        isTextElement("John Doe")
                    )
                ),
                isEmptyHyperlinkedRun(),
                isEmptyRun()
            )));
        }

        @Test
        public void fieldWithoutSeparateFldCharIsIgnored() {
            XmlElement hyperlinkRunXml = runXml("this is a hyperlink");
            XmlElement element = paragraphXml(list(
                BEGIN_COMPLEX_FIELD,
                HYPERLINK_INSTRTEXT,
                SEPARATE_COMPLEX_FIELD,
                BEGIN_COMPLEX_FIELD,
                END_COMPLEX_FIELD,
                hyperlinkRunXml,
                END_COMPLEX_FIELD
            ));
            DocumentElement paragraph = readSuccess(bodyReader(), element);

            assertThat(paragraph, isParagraph(hasChildren(
                isEmptyRun(),
                isEmptyHyperlinkedRun(),
                isEmptyHyperlinkedRun(),
                isEmptyHyperlinkedRun(),
                isHyperlinkedRun(
                    hasHref(URI),
                    hasChildren(
                        isTextElement("this is a hyperlink")
                    )
                ),
                isEmptyRun()
            )));
        }
    }

    @Nested
    public class CheckboxTests {
        @Test
        public void complexFieldCheckboxWithoutSeparateIsRead() {
            XmlElement element = element("w:p", list(
                element("w:r", list(
                    element("w:fldChar", map("w:fldCharType", "begin"))
                )),
                element("w:instrText", list(
                    textXml(" FORMCHECKBOX ")
                )),
                element("w:r", list(
                    element("w:fldChar", map("w:fldCharType", "end"))
                ))
            ));

            DocumentElement paragraph = readSuccess(bodyReader(), element);

            assertThat(paragraph, isParagraph(hasChildren(
                isEmptyRun(),
                isRun(hasChildren(
                    isCheckbox()
                ))
            )));
        }

        @Test
        public void complexFieldCheckboxWithSeparateIsRead() {
            XmlElement element = element("w:p", list(
                element("w:r", list(
                    element("w:fldChar", map("w:fldCharType", "begin"))
                )),
                element("w:instrText", list(
                    textXml(" FORMCHECKBOX ")
                )),
                element("w:r", list(
                    element("w:fldChar", map("w:fldCharType", "separate"))
                )),
                element("w:r", list(
                    element("w:fldChar", map("w:fldCharType", "end"))
                ))
            ));

            DocumentElement paragraph = readSuccess(bodyReader(), element);

            assertThat(paragraph, isParagraph(hasChildren(
                isEmptyRun(),
                isEmptyRun(),
                isRun(hasChildren(
                    isCheckbox()
                ))
            )));
        }

        @Test
        public void complexFieldCheckboxWithoutDefaultNorCheckedIsUnchecked() {
            XmlElement element = complexFieldCheckboxParagraph(list(
                element("w:checkBox")
            ));

            DocumentElement paragraph = readSuccess(bodyReader(), element);

            assertThat(paragraph, isParagraph(hasChildren(
                isEmptyRun(),
                isEmptyRun(),
                isRun(hasChildren(
                    isCheckbox(false)
                ))
            )));
        }

        @Test
        public void complexFieldCheckboxWithDefault0AndWithoutCheckedIsUnchecked() {
            XmlElement element = complexFieldCheckboxParagraph(list(
                element("w:checkBox", list(
                    element("w:default", map("w:val", "0"))
                ))
            ));

            DocumentElement paragraph = readSuccess(bodyReader(), element);

            assertThat(paragraph, isParagraph(hasChildren(
                isEmptyRun(),
                isEmptyRun(),
                isRun(hasChildren(
                    isCheckbox(false)
                ))
            )));
        }

        @Test
        public void complexFieldCheckboxWithDefault1AndWithoutCheckedIsChecked() {
            XmlElement element = complexFieldCheckboxParagraph(list(
                element("w:checkBox", list(
                    element("w:default", map("w:val", "1"))
                ))
            ));

            DocumentElement paragraph = readSuccess(bodyReader(), element);

            assertThat(paragraph, isParagraph(hasChildren(
                isEmptyRun(),
                isEmptyRun(),
                isRun(hasChildren(
                    isCheckbox(true)
                ))
            )));
        }

        @Test
        public void complexFieldCheckboxWithDefault1AndChecked0IsUnchecked() {
            XmlElement element = complexFieldCheckboxParagraph(list(
                element("w:checkBox", list(
                    element("w:default", map("w:val", "1")),
                    element("w:checked", map("w:val", "0"))
                ))
            ));

            DocumentElement paragraph = readSuccess(bodyReader(), element);

            assertThat(paragraph, isParagraph(hasChildren(
                isEmptyRun(),
                isEmptyRun(),
                isRun(hasChildren(
                    isCheckbox(false)
                ))
            )));
        }

        @Test
        public void complexFieldCheckboxWithDefault0AndChecked1IsUnchecked() {
            XmlElement element = complexFieldCheckboxParagraph(list(
                element("w:checkBox", list(
                    element("w:default", map("w:val", "0")),
                    element("w:checked", map("w:val", "1"))
                ))
            ));

            DocumentElement paragraph = readSuccess(bodyReader(), element);

            assertThat(paragraph, isParagraph(hasChildren(
                isEmptyRun(),
                isEmptyRun(),
                isRun(hasChildren(
                    isCheckbox(true)
                ))
            )));
        }

        @Test
        public void structuredDocumentTagCheckboxWithoutCheckedIsNotChecked() {
            XmlElement element = element("w:sdt", list(
                element("w:sdtPr", list(
                    element("wordml:checkbox")
                ))
            ));

            DocumentElement result = readSuccess(bodyReader(), element);

            assertThat(result, isCheckbox(false));
        }

        @Test
        public void structuredDocumentTagCheckboxWithChecked0IsNotChecked() {
            XmlElement element = element("w:sdt", list(
                element("w:sdtPr", list(
                    element("wordml:checkbox", list(
                        element("wordml:checked", map("wordml:val", "0"))
                    ))
                ))
            ));

            DocumentElement result = readSuccess(bodyReader(), element);

            assertThat(result, isCheckbox(false));
        }

        @Test
        public void structuredDocumentTagCheckboxWithChecked1IsChecked() {
            XmlElement element = element("w:sdt", list(
                element("w:sdtPr", list(
                    element("wordml:checkbox", list(
                        element("wordml:checked", map("wordml:val", "1"))
                    ))
                ))
            ));

            DocumentElement result = readSuccess(bodyReader(), element);

            assertThat(result, isCheckbox(true));
        }

        @Test
        public void whenStructuredDocumentTagCheckboxHasSdtContentThenCheckboxReplacesSingleCharacter() {
            XmlElement element = element("w:tbl", list(
                wTr(
                    element("w:sdt", list(
                        element("w:sdtPr", list(
                            element("wordml:checkbox", list(
                                element("wordml:checked", map("wordml:val", "1"))
                            ))
                        )),
                        element("w:sdtContent", list(
                            element("w:tc", list(
                                element("w:p", list(
                                    element("w:r", list(
                                        element("w:t", list(
                                            textXml("☐")
                                        ))
                                    ))
                                ))
                            ))
                        ))
                    ))
                )
            ));

            DocumentElement result = readSuccess(bodyReader(), element);

            assertThat(result, deepEquals(table(list(
                tableRow(list(
                    tableCell(
                        withChildren(
                            paragraph(withChildren(
                                run(withChildren(
                                    checkbox(true)
                                ))
                            ))
                        )
                    )
                ))
            ))));
        }

        @Test
        public void whenStructuredDocumentTagCheckboxHasSdtContentThenDeletedContentIsIgnored() {
            XmlElement element = element("w:tbl", list(
                wTr(
                    element("w:sdt", list(
                        element("w:sdtPr", list(
                            element("wordml:checkbox", list(
                                element("wordml:checked", map("wordml:val", "1"))
                            ))
                        )),
                        element("w:sdtContent", list(
                            element("w:tc", list(
                                element("w:p", list(
                                    element("w:r", list(
                                        element("w:t", list(
                                            textXml("☐")
                                        ))
                                    )),
                                    element("w:del", list(
                                        element("w:r", list(
                                            element("w:t", list(
                                                textXml("☐")
                                            ))
                                        ))
                                    ))
                                ))
                            ))
                        ))
                    ))
                )
            ));

            DocumentElement result = readSuccess(bodyReader(), element);

            assertThat(result, deepEquals(table(list(
                tableRow(list(
                    tableCell(
                        withChildren(
                            paragraph(withChildren(
                                run(withChildren(
                                    checkbox(true)
                                ))
                            ))
                        )
                    )
                ))
            ))));
        }

        private XmlElement complexFieldCheckboxParagraph(List<XmlNode> ffDataChildren) {
            return element("w:p", list(
                element("w:r", list(
                    element("w:fldChar", map("w:fldCharType", "begin"), list(
                        element("w:ffData", ffDataChildren)
                    ))
                )),
                element("w:instrText", list(
                    textXml(" FORMCHECKBOX ")
                )),
                element("w:r", list(
                    element("w:fldChar", map("w:fldCharType", "separate"))
                )),
                element("w:r", list(
                    element("w:fldChar", map("w:fldCharType", "end"))
                ))
            ));
        }
    }

    @Test
    public void runHasNoStyleIfItHasNoProperties() {
        XmlElement element = runXml(list());
        assertThat(
            readSuccess(bodyReader(), element),
            hasStyle(Optional.empty()));
    }

    @Test
    public void whenRunHasStyleIdInStylesThenStyleNameIsReadFromStyles() {
        XmlElement element = runXml(list(
            element("w:rPr", list(
                element("w:rStyle", map("w:val", "Heading1Char"))))));

        Style style = new Style("Heading1Char", Optional.of("Heading 1 Char"));
        Styles styles = new Styles(
            map(),
            map("Heading1Char", style),
            map(),
            map()
        );
        assertThat(
            readSuccess(bodyReader(styles), element),
            hasStyle(Optional.of(style)));
    }

    @Test
    public void warningIsEmittedWhenRunStyleCannotBeFound() {
        XmlElement element = runXml(list(
            element("w:rPr", list(
                element("w:rStyle", map("w:val", "Heading1Char"))))));

        assertThat(
            read(bodyReader(), element),
            isInternalResult(
                hasStyle(Optional.of(new Style("Heading1Char", Optional.empty()))),
                list("Run style with ID Heading1Char was referenced but not defined in the document")));
    }

    @Test
    public void runIsNotBoldIfBoldElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("bold", equalTo(false)));
    }

    @Test
    public void runIsBoldIfBoldElementIsPresent() {
        XmlElement element = runXmlWithProperties(element("w:b"));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("bold", equalTo(true)));
    }

    @Test
    public void runIsNotItalicIfItalicElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("italic", equalTo(false)));
    }

    @Test
    public void runIsItalicIfItalicElementIsPresent() {
        XmlElement element = runXmlWithProperties(element("w:i"));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("italic", equalTo(true)));
    }

    @Test
    public void runIsNotUnderlinedIfUnderlineElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("underline", equalTo(false))
        );
    }

    @Test
    public void runIsNotUnderlinedIfUnderlineElementIsPresentWithoutValAttribute() {
        XmlElement element = runXmlWithProperties(element("w:u"));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("underline", equalTo(false))
        );
    }

    @Test
    public void runIsNotUnderlinedIfUnderlineElementIsPresentAndValIsFalse() {
        XmlElement element = runXmlWithProperties(element("w:u", map("w:val", "false")));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("underline", equalTo(false))
        );
    }

    @Test
    public void runIsNotUnderlinedIfUnderlineElementIsPresentAndValIs0() {
        XmlElement element = runXmlWithProperties(element("w:u", map("w:val", "0")));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("underline", equalTo(false))
        );
    }

    @Test
    public void runIsNotUnderlinedIfUnderlineElementIsPresentAndValIsNone() {
        XmlElement element = runXmlWithProperties(element("w:u", map("w:val", "none")));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("underline", equalTo(false))
        );
    }

    @Test
    public void runIsUnderlinedIfUnderlineElementIsPresentAndValIsNotNoneNorFalsy() {
        XmlElement element = runXmlWithProperties(element("w:u", map("w:val", "single")));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("underline", equalTo(true))
        );
    }

    @Test
    public void runIsNotStruckthroughIfStrikethroughElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("strikethrough", equalTo(false)));
    }

    @Test
    public void runIsStruckthroughIfStrikethroughElementIsPresent() {
        XmlElement element = runXmlWithProperties(element("w:strike"));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("strikethrough", equalTo(true)));
    }

    @Test
    public void runIsNotSmallCapsIfSmallCapsElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("smallCaps", equalTo(false)));
    }

    @Test
    public void runIsSmallCapsIfSmallCapsElementIsPresent() {
        XmlElement element = runXmlWithProperties(element("w:smallCaps"));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("smallCaps", equalTo(true)));
    }

    @Nested
    public class RunBooleanPropertyTests {
        private class TestCase {
            final String propertyName;
            final String tagName;

            TestCase(String propertyName, String tagName) {
                this.propertyName = propertyName;
                this.tagName = tagName;
            }
        }

        private final List<TestCase> TEST_CASES = list(
            new TestCase("bold", "w:b"),
            new TestCase("underline", "w:u"),
            new TestCase("italic", "w:i"),
            new TestCase("strikethrough", "w:strike"),
            new TestCase("allCaps", "w:caps"),
            new TestCase("smallCaps", "w:smallCaps")
        );

        @TestFactory
        public List<DynamicTest> runBooleanPropertyIsFalseIfElementIsPresentAndValIsFalse() {
            return eagerMap(TEST_CASES, testCase -> DynamicTest.dynamicTest(
                testCase.propertyName + " property is false if " + testCase.tagName + " element is present and val is false",
                () -> {
                    XmlElement element = runXmlWithProperties(
                        element(testCase.tagName, map("w:val", "false"))
                    );

                    assertThat(
                        readSuccess(bodyReader(), element),
                        hasProperty(testCase.propertyName, equalTo(false)));
                })
            );
        }

        @TestFactory
        public List<DynamicTest> runBooleanPropertyIsFalseIfElementIsPresentAndValIs0() {
            return eagerMap(TEST_CASES, testCase -> DynamicTest.dynamicTest(
                testCase.propertyName + " property is false if " + testCase.tagName + " element is present and val is 0",
                () -> {
                    XmlElement element = runXmlWithProperties(
                        element(testCase.tagName, map("w:val", "0"))
                    );

                    assertThat(
                        readSuccess(bodyReader(), element),
                        hasProperty(testCase.propertyName, equalTo(false)));
                })
            );
        }

        @TestFactory
        public List<DynamicTest> runBooleanPropertyIsFalseIfElementIsPresentAndValIsTrue() {
            return eagerMap(TEST_CASES, testCase -> DynamicTest.dynamicTest(
                testCase.propertyName + " property is true if " + testCase.tagName + " element is present and val is true",
                () -> {
                    XmlElement element = runXmlWithProperties(
                        element(testCase.tagName, map("w:val", "true"))
                    );

                    assertThat(
                        readSuccess(bodyReader(), element),
                        hasProperty(testCase.propertyName, equalTo(true)));
                })
            );
        }

        @TestFactory
        public List<DynamicTest> runBooleanPropertyIsFalseIfElementIsPresentAndValIs1() {
            return eagerMap(TEST_CASES, testCase -> DynamicTest.dynamicTest(
                testCase.propertyName + " property is false if " + testCase.tagName + " element is present and val is 1",
                () -> {
                    XmlElement element = runXmlWithProperties(
                        element(testCase.tagName, map("w:val", "1"))
                    );

                    assertThat(
                        readSuccess(bodyReader(), element),
                        hasProperty(testCase.propertyName, equalTo(true)));
                })
            );
        }
    }

    @Test
    public void runHasBaselineVerticalAlignmentIfVerticalAlignmentElementIsNotPresent() {
        XmlElement element = runXmlWithProperties();

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("verticalAlignment", equalTo(VerticalAlignment.BASELINE)));
    }

    @Test
    public void runIsSuperscriptIfVerticalAlignmentPropertyIsSetToSuperscript() {
        XmlElement element = runXmlWithProperties(
            element("w:vertAlign", map("w:val", "superscript")));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("verticalAlignment", equalTo(VerticalAlignment.SUPERSCRIPT)));
    }

    @Test
    public void runIsSubscriptIfVerticalAlignmentPropertyIsSetToSubscript() {
        XmlElement element = runXmlWithProperties(
            element("w:vertAlign", map("w:val", "subscript")));

        assertThat(
            readSuccess(bodyReader(), element),
            hasProperty("verticalAlignment", equalTo(VerticalAlignment.SUBSCRIPT)));
    }

    @Test
    public void runHasNoHighlightByDefault() {
        XmlElement element = runXmlWithProperties();

        DocumentElement result = readSuccess(bodyReader(), element);

        assertThat(result, hasProperty("highlight", equalTo(Optional.empty())));
    }

    @Test
    public void runHasHighlightReadFromProperties() {
        XmlElement element = runXmlWithProperties(
            element("w:highlight", map("w:val", "yellow"))
        );

        DocumentElement result = readSuccess(bodyReader(), element);

        assertThat(result, hasProperty("highlight", equalTo(Optional.of("yellow"))));
    }

    @Test
    public void whenHighlightIsNoneThenRunhasNoHighlight() {
        XmlElement element = runXmlWithProperties(
            element("w:highlight", map("w:val", "none"))
        );

        DocumentElement result = readSuccess(bodyReader(), element);

        assertThat(result, hasProperty("highlight", equalTo(Optional.empty())));
    }

    @Test
    public void canReadTabElement() {
        XmlElement element = element("w:tab");

        assertThat(
            readSuccess(bodyReader(), element),
            equalTo(Tab.TAB));
    }

    @Test
    public void noBreakHyphenElementIsReadAsNonBreakingHyphenCharacter() {
        XmlElement element = element("w:noBreakHyphen");

        assertThat(
            readSuccess(bodyReader(), element),
            isTextElement("\u2011")
        );
    }

    @Test
    public void softHyphenElementIsReadAsSoftHyphenCharacter() {
        XmlElement element = element("w:softHyphen");

        assertThat(
            readSuccess(bodyReader(), element),
            isTextElement("\u00ad")
        );
    }

    @Test
    public void symWithSupportedFontAndSupportedCodePointInAsciiRangeIsConvertedToText() {
        XmlElement element = element("w:sym", map("w:font", "Wingdings", "w:char", "28"));

        DocumentElement result = readSuccess(bodyReader(), element);

        assertThat(
            result,
            isTextElement("\uD83D\uDD7F")
        );
    }

    @Test
    public void symWithSupportedFontAndSupportedCodePointInPrivateUseAreaIsConvertedToText() {
        XmlElement element = element("w:sym", map("w:font", "Wingdings", "w:char", "F028"));

        DocumentElement result = readSuccess(bodyReader(), element);

        assertThat(
            result,
            isTextElement("\uD83D\uDD7F")
        );
    }

    @Test
    public void symWithUnsupportedFontAndCodePointProducesEmptyResultWithWarning() {
        XmlElement element = element("w:sym", map("w:font", "Dingwings", "w:char", "28"));

        InternalResult<List<DocumentElement>> result = readAll(bodyReader(), element);

        assertThat(result, isInternalResult(
            equalTo(list()),
            list("A w:sym element with an unsupported character was ignored: char 28 in font Dingwings")
        ));
    }

    @Test
    public void brWithoutExplicitTypeIsReadAsLineBreak() {
        XmlElement element = element("w:br");

        assertThat(
            readSuccess(bodyReader(), element),
            equalTo(Break.LINE_BREAK));
    }

    @Test
    public void brWithTextWrappingTypeIsReadAsLineBreak() {
        XmlElement element = element("w:br", map("w:type", "textWrapping"));

        assertThat(
            readSuccess(bodyReader(), element),
            equalTo(Break.LINE_BREAK)
        );
    }

    @Test
    public void brWithPageTypeIsReadAsPageBreak() {
        XmlElement element = element("w:br", map("w:type", "page"));

        assertThat(
            readSuccess(bodyReader(), element),
            equalTo(Break.PAGE_BREAK)
        );
    }

    @Test
    public void brWithColumnTypeIsReadAsColumnBreak() {
        XmlElement element = element("w:br", map("w:type", "column"));

        assertThat(
            readSuccess(bodyReader(), element),
            equalTo(Break.COLUMN_BREAK)
        );
    }

    @Test
    public void warningOnBreaksThatArentRecognised() {
        XmlElement element = element("w:br", map("w:type", "unknownBreakType"));

        assertThat(
            readAll(bodyReader(), element),
            isInternalResult(equalTo(list()), list("Unsupported break type: unknownBreakType")));
    }

    @Test
    public void canReadTableElements() {
        XmlElement element = element("w:tbl", list(
            element("w:tr", list(
                element("w:tc", list(
                    element("w:p")))))));

        assertThat(
            readSuccess(bodyReader(), element),
            isTable(hasChildren(
                deepEquals(tableRow(list(
                    new TableCell(1, 1, list(
                        paragraph()
                    ))
                )))
            ))
        );
    }

    @Test
    public void tableHasNoStyleIfItHasNoProperties() {
        XmlElement element = element("w:tbl");
        assertThat(
            readSuccess(bodyReader(), element),
            hasStyle(Optional.empty())
        );
    }

    @Test
    public void whenTableHasStyleIdInStylesThenStyleNameIsReadFromStyles() {
        XmlElement element = element("w:tbl", list(
            element("w:tblPr", list(
                element("w:tblStyle", map("w:val", "TableNormal"))
            ))
        ));

        Style style = new Style("TableNormal", Optional.of("Normal Table"));
        Styles styles = new Styles(
            map(),
            map(),
            map("TableNormal", style),
            map()
        );
        assertThat(
            readSuccess(bodyReader(styles), element),
            hasStyle(Optional.of(style))
        );
    }

    @Test
    public void warningIsEmittedWhenTableStyleCannotBeFound() {
        XmlElement element = element("w:tbl", list(
            element("w:tblPr", list(
                element("w:tblStyle", map("w:val", "TableNormal"))
            ))
        ));

        assertThat(
            read(bodyReader(), element),
            isInternalResult(
                hasStyle(Optional.of(new Style("TableNormal", Optional.empty()))),
                list("Table style with ID TableNormal was referenced but not defined in the document")
            )
        );
    }

    @Test
    public void tblHeaderMarksTableRowAsHeader() {
        XmlElement element = element("w:tbl", list(
            element("w:tr", list(
                element("w:trPr", list(
                    element("w:tblHeader")
                ))
            )),
            element("w:tr")
        ));

        assertThat(
            readSuccess(bodyReader(), element),
            isTable(hasChildren(
                isRow(isHeader(true)),
                isRow(isHeader(false))
            ))
        );
    }

    @Test
    public void gridspanIsReadAsColspanForTableCell() {
        XmlElement element = element("w:tbl", list(
            element("w:tr", list(
                element("w:tc", list(
                    element("w:tcPr", list(
                        element("w:gridSpan", map("w:val", "2"))
                    )),
                    element("w:p")))))));

        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(table(list(
                tableRow(list(
                    new TableCell(1, 2, list(
                        paragraph()
                    ))
                ))
            )))
        );
    }

    @Test
    public void vmergeIsReadAsRowspanForTableCell() {
        XmlElement element = element("w:tbl", list(
            wTr(wTc()),
            wTr(wTc(wTcPr(wVmerge("restart")))),
            wTr(wTc(wTcPr(wVmerge("continue")))),
            wTr(wTc(wTcPr(wVmerge("continue")))),
            wTr(wTc())
        ));

        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(table(list(
                tableRow(list(new TableCell(1, 1, list()))),
                tableRow(list(new TableCell(3, 1, list()))),
                tableRow(list()),
                tableRow(list()),
                tableRow(list(new TableCell(1, 1, list())))
            )))
        );
    }

    @Test
    public void vmergeWithoutValIsTreatedAsContinue() {
        XmlElement element = element("w:tbl", list(
            wTr(wTc(wTcPr(wVmerge("restart")))),
            wTr(wTc(wTcPr(element("w:vMerge"))))
        ));

        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(table(list(
                tableRow(list(new TableCell(2, 1, list()))),
                tableRow(list())
            )))
        );
    }

    @Test
    public void vmergeAccountsForCellsSpanningColumns() {
        XmlElement element = element("w:tbl", list(
            wTr(wTc(), wTc(), wTc(wTcPr(wVmerge("restart")))),
            wTr(wTc(wTcPr(wGridspan("2"))), wTc(wTcPr(wVmerge("continue")))),
            wTr(wTc(), wTc(), wTc(wTcPr(wVmerge("continue")))),
            wTr(wTc(), wTc(), wTc())
        ));

        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(table(list(
                tableRow(list(new TableCell(1, 1, list()), new TableCell(1, 1, list()), new TableCell(3, 1, list()))),
                tableRow(list(new TableCell(1, 2, list()))),
                tableRow(list(new TableCell(1, 1, list()), new TableCell(1, 1, list()))),
                tableRow(list(new TableCell(1, 1, list()), new TableCell(1, 1, list()), new TableCell(1, 1, list())))
            )))
        );
    }

    @Test
    public void noVerticalCellMergingIfMergedCellsDoNotLineUp() {
        XmlElement element = element("w:tbl", list(
            wTr(wTc(wTcPr(wGridspan("2"))), wTc(wTcPr(wVmerge("restart")))),
            wTr(wTc(), wTc(wTcPr(wVmerge("continue"))))
        ));

        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(table(list(
                tableRow(list(new TableCell(1, 2, list()), new TableCell(1, 1, list()))),
                tableRow(list(new TableCell(1, 1, list()), new TableCell(1, 1, list())))
            )))
        );
    }

    @Test
    public void whenRowIsMarkedAsDeletedInRowPropertiesThenRowIsIgnored() {
        XmlElement element = element("w:tbl", list(
            element("w:tr", list(
                element("w:tc", list(
                    element("w:p", list(
                        runXml("Row 1")
                    ))
                ))
            )),

            element("w:tr", list(
                element("w:trPr", list(
                    element("w:del")
                )),
                element("w:tc", list(
                    element("w:p", list(
                        runXml("Row 2")
                    ))
                ))
            ))
        ));

        DocumentElement result = readSuccess(bodyReader(), element);

        assertThat(
            result,
            deepEquals(table(list(
                tableRow(list(
                    tableCell(
                        withChildren(
                            paragraph(withChildren(
                                runWithText("Row 1")
                            ))
                        )
                    )
                ))
            )))
        );
    }

    @Test
    public void warningIfNonRowInTable() {
        // Include normal rows to ensure they're still read correctly.
        XmlElement element = element("w:tbl", list(
            element("w:tr", list(
                element("w:tc", list(
                    element("w:p", list(
                        runXml("Row 1")
                    ))
                ))
            )),
            element("w:p"),
            element("w:tr", list(
                element("w:tc", list(
                    element("w:p", list(
                        runXml("Row 2")
                    ))
                ))
            ))
        ));

        InternalResult<DocumentElement> result = read(bodyReader(), element);

        assertThat(
            result,
            isInternalResult(
                deepEquals(table(list(
                    tableRow(list(
                        tableCell(withChildren(
                            paragraph(withChildren(
                                runWithText("Row 1")
                            ))
                        ))
                    )),
                    paragraph(),
                    tableRow(list(
                        tableCell(withChildren(
                            paragraph(withChildren(
                                runWithText("Row 2")
                            ))
                        ))
                    ))
                ))),
                list("unexpected non-row element in table, cell merging may be incorrect")
            )
        );
    }

    @Test
    public void warningIfNonCellInTableRow() {
        // Include normal cells to ensure they're still read correctly.
        XmlElement element = element("w:tbl", list(
            wTr(
                element("w:tc", list(
                    element("w:p", list(
                        runXml("Cell 1")
                    ))
                )),
                element("w:p"),
                element("w:tc", list(
                    element("w:p", list(
                        runXml("Cell 2")
                    ))
                ))
            )
        ));

        assertThat(
            read(bodyReader(), element),
            isInternalResult(
                deepEquals(table(list(
                    tableRow(list(
                        tableCell(withChildren(
                            paragraph(withChildren(
                                runWithText("Cell 1")
                            ))
                        )),
                        paragraph(),
                        tableCell(withChildren(
                            paragraph(withChildren(
                                runWithText("Cell 2")
                            ))
                        ))
                    ))
                ))),
                list("unexpected non-cell element in table row, cell merging may be incorrect")
            )
        );
    }

    @Test
    public void hyperlinkIsReadAsExternalHyperlinkIfItHasARelationshipId() {
        Relationships relationships = new Relationships(list(
            hyperlinkRelationship("r42", "http://example.com")
        ));
        XmlElement element = element("w:hyperlink", map("r:id", "r42"), list(runXml(list())));
        assertThat(
            readSuccess(bodyReader(relationships), element),
            isHyperlink(
                hasHref("http://example.com"),
                hasNoAnchor(),
                hasChildren(isRun(hasChildren()))
            )
        );
    }

    @Test
    public void hyperlinkIsReadAsExternalHyperlinkIfItHasARelationshipIdAndAnAnchor() {
        Relationships relationships = new Relationships(list(
            hyperlinkRelationship("r42", "http://example.com/")
        ));
        XmlElement element = element("w:hyperlink", map("r:id", "r42", "w:anchor", "fragment"), list(runXml(list())));
        assertThat(
            readSuccess(bodyReader(relationships), element),
            isHyperlink(hasHref("http://example.com/#fragment"))
        );
    }

    @Test
    public void hyperlinkExistingFragmentIsReplacedWhenAnchorIsSetOnExternalLink() {
        Relationships relationships = new Relationships(list(
            hyperlinkRelationship("r42", "http://example.com/#previous")
        ));
        XmlElement element = element("w:hyperlink", map("r:id", "r42", "w:anchor", "fragment"), list(runXml(list())));
        assertThat(
            readSuccess(bodyReader(relationships), element),
            isHyperlink(hasHref("http://example.com/#fragment"))
        );
    }

    @Test
    public void hyperlinkIsReadAsInternalHyperlinkIfItHasAnAnchorAttribute() {
        XmlElement element = element("w:hyperlink", map("w:anchor", "start"), list(runXml(list())));
        assertThat(
            readSuccess(bodyReader(), element),
            isHyperlink(
                hasAnchor("start"),
                hasNoHref()
            )
        );
    }

    @Test
    public void hyperlinkIsIgnoredIfItDoesNotHaveARelationshipIdNorAnchor() {
        XmlElement element = element("w:hyperlink", list(runXml(list())));
        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(run(withChildren())));
    }

    @Test
    public void hyperlinkTargetFrameIsRead() {
        XmlElement element = element("w:hyperlink", map(
            "w:anchor", "start",
            "w:tgtFrame", "_blank"
        ));
        assertThat(
            readSuccess(bodyReader(), element),
            isHyperlink(hasTargetFrame("_blank"))
        );
    }

    @Test
    public void hyperlinkEmptyTargetFrameIsIgnored() {
        XmlElement element = element("w:hyperlink", map(
            "w:anchor", "start",
            "w:tgtFrame", ""
        ));
        assertThat(
            readSuccess(bodyReader(), element),
            isHyperlink(hasNoTargetFrame())
        );
    }

    @Test
    public void goBackBookmarkIsIgnored() {
        XmlElement element = element("w:bookmarkStart", map("w:name", "_GoBack"));
        assertThat(
            readAll(bodyReader(), element),
            isInternalResult(equalTo(list()), list()));
    }

    @Test
    public void bookmarkStartIsReadIfNameIsNotGoBack() {
        XmlElement element = element("w:bookmarkStart", map("w:name", "start"));
        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(new Bookmark("start")));
    }

    @Test
    public void footnoteReferenceHasIdRead() {
        XmlElement element = element("w:footnoteReference", map("w:id", "4"));
        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(footnoteReference("4")));
    }

    @Test
    public void endnoteReferenceHasIdRead() {
        XmlElement element = element("w:endnoteReference", map("w:id", "4"));
        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(endnoteReference("4")));
    }

    @Test
    public void commentReferenceHasIdRead() {
        XmlElement element = element("w:commentReference", map("w:id", "4"));
        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(new CommentReference("4")));
    }

    @Test
    public void textBoxesHaveContentAppendedAfterContainingParagraph() {
        XmlElement textBox = element("w:pict", list(
            element("v:shape", list(
                element("v:textbox", list(
                    element("w:txbxContent", list(
                        paragraphXml(list(
                            runXml(list(textXml("[textbox-content]")))))))))))));
        XmlElement paragraph = paragraphXml(list(
            runXml(list(textXml("[paragragh start]"))),
            runXml(list(textBox, textXml("[paragragh end]")))));

        List<DocumentElement> expected = list(
            paragraph(withChildren(
                run(withChildren(
                    new Text("[paragragh start]"))),
                run(withChildren(
                    new Text("[paragragh end]"))))),
            paragraph(withChildren(
                run(withChildren(
                    new Text("[textbox-content]"))))));

        assertThat(
            readAll(bodyReader(), paragraph),
            isInternalResult(deepEquals(expected), list()));
    }


    private static final String IMAGE_BYTES = "Not an image at all!";
    private static final String IMAGE_RELATIONSHIP_ID = "rId5";


    @Test
    public void canReadImagedataElementsWithIdAttribute() throws IOException {
        assertCanReadEmbeddedImage(image ->
            element("v:imagedata", map("r:id", image.relationshipId, "o:title", image.altText)));
    }

    @Test
    public void whenImagedataElementHasNoRelationshipIdThenItIsIgnoredWithWarning() throws IOException {
        XmlElement element = element("v:imagedata");

        assertThat(
            readAll(bodyReader(), element),
            isInternalResult(equalTo(list()), list("A v:imagedata element without a relationship ID was ignored")));
    }

    @Test
    public void canReadInlinePictures() throws IOException {
        assertCanReadEmbeddedImage(image ->
            inlineImageXml(embeddedBlipXml(image.relationshipId), image.altText));
    }

    @Test
    public void altTextTitleIsUsedIfAltTextDescriptionIsMissing() throws IOException {
        XmlElement element = inlineImageXml(
            embeddedBlipXml(IMAGE_RELATIONSHIP_ID),
            Optional.empty(),
            Optional.of("It's a hat")
        );

        Image image = readEmbeddedImage(element);

        assertThat(image, hasProperty("altText", deepEquals(Optional.of("It's a hat"))));
    }

    @Test
    public void altTextTitleIsUsedIfAltTextDescriptionIsBlank() throws IOException {
        XmlElement element = inlineImageXml(
            embeddedBlipXml(IMAGE_RELATIONSHIP_ID),
            Optional.of(" "),
            Optional.of("It's a hat")
        );

        Image image = readEmbeddedImage(element);

        assertThat(image, hasProperty("altText", deepEquals(Optional.of("It's a hat"))));
    }

    @Test
    public void altTextDescriptionIsPreferredToAltTextTitle() throws IOException {
        XmlElement element = inlineImageXml(
            embeddedBlipXml(IMAGE_RELATIONSHIP_ID),
            Optional.of("It's a hat"),
            Optional.of("hat")
        );

        Image image = readEmbeddedImage(element);

        assertThat(image, hasProperty("altText", deepEquals(Optional.of("It's a hat"))));
    }

    @Test
    public void canReadAnchoredPictures() throws IOException {
        assertCanReadEmbeddedImage(image ->
            anchoredImageXml(embeddedBlipXml(image.relationshipId), image.altText));
    }

    private void assertCanReadEmbeddedImage(Function<EmbeddedImage, XmlElement> generateXml) throws IOException {
        XmlElement element = generateXml.apply(new EmbeddedImage(IMAGE_RELATIONSHIP_ID, "It's a hat"));
        Image image = readEmbeddedImage(element);
        assertThat(image, allOf(
            hasProperty("altText", deepEquals(Optional.of("It's a hat"))),
            hasProperty("contentType", deepEquals(Optional.of("image/png")))));
        assertThat(
            toString(image.open()),
            equalTo(IMAGE_BYTES));
    }

    private Image readEmbeddedImage(XmlElement element) {
        Relationships relationships = new Relationships(list(
            imageRelationship(IMAGE_RELATIONSHIP_ID, "media/hat.png")
        ));
        Archive file = InMemoryArchive.fromStrings(map("word/media/hat.png", IMAGE_BYTES));

        return (Image) readSuccess(
            BodyXmlReaderMakers.bodyReader(relationships, file),
            element);
    }

    private static String toString(InputStream stream) throws IOException {
        return new String(toByteArray(stream), StandardCharsets.UTF_8);
    }

    private class EmbeddedImage {
        private final String relationshipId;
        private final String altText;

        public EmbeddedImage(String relationshipId, String altText) {
            this.relationshipId = relationshipId;
            this.altText = altText;
        }
    }

    @Test
    public void canReadLinkedPictures() throws IOException {
        XmlElement element = inlineImageXml(linkedBlipXml(IMAGE_RELATIONSHIP_ID), "");
        Relationships relationships = new Relationships(list(
            imageRelationship(IMAGE_RELATIONSHIP_ID, "file:///media/hat.png")
        ));

        Image image = (Image) readSuccess(
            bodyReader(relationships, new InMemoryFileReader(map("file:///media/hat.png", IMAGE_BYTES))),
            element);

        assertThat(
            toString(image.open()),
            equalTo(IMAGE_BYTES));
    }

    @Test
    public void warningIfBlipHasNoImageFile() {
        XmlElement element = inlineImageXml(element("a:blip"), "");

        InternalResult<?> result = readAll(bodyReader(), element);

        assertThat(result, isInternalResult(
            equalTo(list()),
            list("Could not find image file for a:blip element")
        ));
    }

    @Test
    public void warningIfImageTypeIsUnsupportedByWebBrowsers() {
        XmlElement element = inlineImageXml(embeddedBlipXml(IMAGE_RELATIONSHIP_ID), "");
        Relationships relationships = new Relationships(list(
            imageRelationship(IMAGE_RELATIONSHIP_ID, "media/hat.emf")
        ));
        Archive file = InMemoryArchive.fromStrings(map("word/media/hat.emf", IMAGE_BYTES));
        ContentTypes contentTypes = new ContentTypes(map("emf", "image/x-emf"), map());

        InternalResult<?> result = read(
            bodyReader(relationships, file, contentTypes),
            element);

        assertThat(
            result,
            hasWarnings(list("Image of type image/x-emf is unlikely to display in web browsers")));
    }

    @Test
    public void noElementsCreatedIfImageCannotBeFoundInWDrawing() {
        XmlElement element = element("w:drawing");

        InternalResult<List<DocumentElement>> result = readAll(bodyReader(), element);

        assertThat(result, isInternalSuccess(empty()));
    }

    @Test
    public void noElementsCreatedIfImageCannotBeFoundInWpInline() {
        XmlElement element = element("wp:inline");

        InternalResult<List<DocumentElement>> result = readAll(bodyReader(), element);

        assertThat(result, isInternalSuccess(empty()));
    }

    private XmlElement inlineImageXml(XmlElement blip, String description) {
        return inlineImageXml(blip, Optional.of(description), Optional.empty());
    }

    private XmlElement inlineImageXml(XmlElement blip, Optional<String> description, Optional<String> title) {
        return element("w:drawing", list(
            element("wp:inline", imageXml(blip, description, title))));
    }

    private XmlElement anchoredImageXml(XmlElement blip, String description) {
        return element("w:drawing", list(
            element("wp:anchor", imageXml(blip, Optional.of(description), Optional.empty()))));
    }

    private List<XmlNode> imageXml(XmlElement blip, Optional<String> description, Optional<String> title) {
        Map<String, String> properties = new HashMap<>();
        description.ifPresent(value -> properties.put("descr", value));
        title.ifPresent(value -> properties.put("title", value));

        return list(
            element("wp:docPr", properties),
            element("a:graphic", list(
                element("a:graphicData", list(
                    element("pic:pic", list(
                        element("pic:blipFill", list(blip)))))))));
    }

    private XmlElement embeddedBlipXml(String relationshipId) {
        return blipXml(map("r:embed", relationshipId));
    }

    private XmlElement linkedBlipXml(String relationshipId) {
        return blipXml(map("r:link", relationshipId));
    }

    private XmlElement blipXml(Map<String, String> attributes) {
        return element("a:blip", attributes);
    }

    @Test
    public void sdtIsReadUsingSdtContent() throws IOException {
        XmlElement element = element("w:sdt", list(element("w:sdtContent", list(textXml("Blackdown")))));

        assertThat(
            readAll(bodyReader(), element),
            isInternalSuccess(deepEquals(list(text("Blackdown")))));
    }

    @Test
    public void appropriateElementsHaveTheirChildrenReadNormally() {
        assertChildrenAreReadNormally("w:ins");
        assertChildrenAreReadNormally("w:object");
        assertChildrenAreReadNormally("w:smartTag");
        assertChildrenAreReadNormally("w:drawing");
        assertChildrenAreReadNormally("v:group");
        assertChildrenAreReadNormally("v:rect");
        assertChildrenAreReadNormally("v:roundrect");
        assertChildrenAreReadNormally("v:shape");
        assertChildrenAreReadNormally("v:textbox");
        assertChildrenAreReadNormally("w:txbxContent");
    }

    private void assertChildrenAreReadNormally(String name) {
        XmlElement element = element(name, list(paragraphXml()));

        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(paragraph()));
    }

    @Test
    public void ignoredElementsAreIgnoredWithoutWarning() {
        assertIsIgnored("office-word:wrap");
        assertIsIgnored("v:shadow");
        assertIsIgnored("v:shapetype");
        assertIsIgnored("w:bookmarkEnd");
        assertIsIgnored("w:sectPr");
        assertIsIgnored("w:proofErr");
        assertIsIgnored("w:lastRenderedPageBreak");
        assertIsIgnored("w:commentRangeStart");
        assertIsIgnored("w:commentRangeEnd");
        assertIsIgnored("w:del");
        assertIsIgnored("w:footnoteRef");
        assertIsIgnored("w:endnoteRef");
        assertIsIgnored("w:annotationRef");
        assertIsIgnored("w:pPr");
        assertIsIgnored("w:rPr");
        assertIsIgnored("w:tblPr");
        assertIsIgnored("w:tblGrid");
        assertIsIgnored("w:tcPr");
    }

    private void assertIsIgnored(String name) {
        XmlElement element = element(name, list(paragraphXml()));

        assertThat(
            readAll(bodyReader(), element),
            isInternalResult(equalTo(list()), list()));
    }

    @Test
    public void unrecognisedElementsAreIgnoredWithWarning() {
        XmlElement element = element("w:huh");
        assertThat(
            readAll(bodyReader(), element),
            isInternalResult(equalTo(list()), list("An unrecognised element was ignored: w:huh")));
    }

    @Test
    public void textNodesAreIgnoredWhenReadingChildren() {
        XmlElement element = runXml(list(XmlNodes.text("[text]")));
        assertThat(
            readSuccess(bodyReader(), element),
            deepEquals(run(withChildren())));
    }

    private static DocumentElement readSuccess(BodyXmlReader reader, XmlElement element) {
        InternalResult<DocumentElement> result = read(reader, element);
        assertThat(result.getWarnings(), emptyIterable());
        return result.getValue();
    }

    private static InternalResult<DocumentElement> read(BodyXmlReader reader, XmlElement element) {
        InternalResult<List<DocumentElement>> result = readAll(reader, element);
        assertThat(result.getValue(), Matchers.hasSize(1));
        return result.map(elements -> elements.get(0));
    }

    private static InternalResult<List<DocumentElement>> readAll(BodyXmlReader reader, XmlElement element) {
        return reader.readElement(element).toResult();
    }

    private XmlElement paragraphXml() {
        return paragraphXml(list());
    }

    private static XmlElement paragraphXml(List<XmlNode> children) {
        return element("w:p", children);
    }

    private static XmlElement runXml(String text) {
        return runXml(list(textXml(text)));
    }

    private static XmlElement runXml(List<XmlNode> children) {
        return element("w:r", children);
    }

    private static XmlElement runXmlWithProperties(XmlNode... children) {
        return element("w:r", list(element("w:rPr", asList(children))));
    }

    private static XmlElement textXml(String value) {
        return element("w:t", list(XmlNodes.text(value)));
    }

    private Matcher<? super DocumentElement> hasStyle(Optional<Style> expected) {
        return hasProperty("style", deepEquals(expected));
    }

    private Matcher<? super DocumentElement> hasNumbering(NumberingLevel expected) {
        return hasNumbering(Optional.of(expected));
    }

    private Matcher<? super DocumentElement> hasNumbering(Optional<NumberingLevel> expected) {
        return hasProperty("numbering", deepEquals(expected));
    }

    private Matcher<? super DocumentElement> hasIndent(Matcher<ParagraphIndent> expected) {
        return hasProperty("indent", expected);
    }

    private Matcher<ParagraphIndent> hasIndentStart(Optional<String> value) {
        return hasProperty("start", equalTo(value));
    }

    private Matcher<ParagraphIndent> hasIndentEnd(Optional<String> value) {
        return hasProperty("end", equalTo(value));
    }

    private Matcher<ParagraphIndent> hasIndentFirstLine(Optional<String> value) {
        return hasProperty("firstLine", equalTo(value));
    }

    private Matcher<ParagraphIndent> hasIndentHanging(Optional<String> value) {
        return hasProperty("hanging", equalTo(value));
    }

    private static Text text(String value) {
        return new Text(value);
    }

    private static Relationship hyperlinkRelationship(String relationshipId, String target) {
        return new Relationship(relationshipId, target, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink");
    }

    private static Relationship imageRelationship(String relationshipId, String target) {
        return new Relationship(relationshipId, target, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image");
    }

    private static Numbering numberingMap(Map<String, Map<String, Numbering.AbstractNumLevel>> numbering) {
        return new Numbering(
            numbering.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey(),
                entry -> new Numbering.AbstractNum(entry.getValue(), Optional.empty())
            )),
            numbering.keySet().stream().collect(Collectors.toMap(
                numId -> numId,
                numId -> new Numbering.Num(Optional.of(numId))
            )),
            Styles.EMPTY
        );
    }
}
