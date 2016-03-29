package org.zwobble.mammoth.internal.conversion;

import org.zwobble.mammoth.internal.documents.*;
import org.zwobble.mammoth.internal.html.Html;
import org.zwobble.mammoth.internal.html.HtmlNode;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.styles.HtmlPath;
import org.zwobble.mammoth.internal.styles.StyleMap;
import org.zwobble.mammoth.internal.util.Lists;

import java.io.IOException;
import java.util.*;

import static org.zwobble.mammoth.internal.util.Lists.*;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.internal.util.Streams.toByteArray;

public class DocumentToHtml {
    public static InternalResult<List<HtmlNode>> convertToHtml(Document document, DocumentToHtmlOptions options) {
        DocumentToHtml documentConverter = new DocumentToHtml(options);
        return documentConverter.convertToHtml(document).toInternalResult();
    }

    private static List<Note> findNotes(Document document, Iterable<NoteReference> noteReferences) {
        return eagerMap(
            noteReferences,
            // TODO: handle missing notes
            reference -> document.getNotes().findNote(reference.getNoteType(), reference.getNoteId()).get());
    }

    public static InternalResult<List<HtmlNode>> convertToHtml(DocumentElement element, DocumentToHtmlOptions options) {
        DocumentToHtml documentConverter = new DocumentToHtml(options);
        return documentConverter.convertToHtml(element).toInternalResult();
    }

    private final String idPrefix;
    private final boolean preserveEmptyParagraphs;
    private final StyleMap styleMap;

    private DocumentToHtml(DocumentToHtmlOptions options) {
        this.idPrefix = options.idPrefix();
        this.preserveEmptyParagraphs = options.shouldPreserveEmptyParagraphs();
        this.styleMap = options.styleMap();
    }

    private ConversionResult convertToHtml(Document document) {
        return convertChildrenToHtml(document).flatMap(mainBody -> {
            // TODO: can you have note references inside a note?
            List<Note> notes = findNotes(document, noteReferences);
            return ConversionResult.flatMap(notes, this::convertToHtml)
                .map(noteNodes -> eagerConcat(mainBody, list(Html.element("ol", noteNodes))));
        });
    }

    private ConversionResult convertToHtml(Note note) {
        String id = generateNoteHtmlId(note.getNoteType(), note.getId());
        String referenceId = generateNoteRefHtmlId(note.getNoteType(), note.getId());
        return convertToHtml(note.getBody()).map(noteBody -> {

            // TODO: we probably want this to collapse more eagerly than other collapsible elements
            // -- for instance, any paragraph will probably do, regardless of attributes. (Possible other elements will do too.)
            HtmlNode backLink = Html.collapsibleElement("p", list(
                Html.text(" "),
                Html.element("a", map("href", "#" + referenceId), list(Html.text("↑")))));
            return list(Html.element("li", map("id", id), eagerConcat(noteBody, list(backLink))));
        });
    }

    private ConversionResult convertToHtml(List<DocumentElement> elements) {
        return ConversionResult.flatMap(
            elements,
            this::convertToHtml);
    }

    private ConversionResult convertChildrenToHtml(HasChildren element) {
        return convertToHtml(element.getChildren());
    }

    private ConversionResult convertToHtml(DocumentElement element) {
        return element.accept(new DocumentElementVisitor<ConversionResult>() {
            @Override
            public ConversionResult visit(Paragraph paragraph) {
                return ConversionResult.map(
                    findHtmlPathForParagraph(paragraph),
                    convertChildrenToHtml(paragraph),
                    (mapping, content) -> {
                        List<HtmlNode> children = preserveEmptyParagraphs ? cons(Html.FORCE_WRITE, content) : content;
                        return mapping.wrap(children);
                    }
                );
            }

            private InternalResult<HtmlPath> findHtmlPathForParagraph(Paragraph paragraph) {
                return styleMap.getParagraphHtmlPath(paragraph)
                    .map(InternalResult::success)
                    .orElseGet(() -> {
                        List<String> warnings = paragraph.getStyle().isPresent()
                            ? list("Unrecognised paragraph style: " + paragraph.getStyle().get().describe())
                            : list();
                        return new InternalResult<>(HtmlPath.element("p"), warnings);
                    });
            }


            @Override
            public ConversionResult visit(Run run) {
                List<HtmlNode> nodes = convertChildrenToHtml(run);
                if (run.isStrikethrough()) {
                    nodes = styleMap.getStrikethrough().orElse(HtmlPath.collapsibleElement("s")).wrap(nodes);
                }
                if (run.isUnderline()) {
                    nodes = styleMap.getUnderline().orElse(HtmlPath.EMPTY).wrap(nodes);
                }
                if (run.getVerticalAlignment() == VerticalAlignment.SUBSCRIPT) {
                    nodes = list(Html.collapsibleElement("sub", nodes));
                }
                if (run.getVerticalAlignment() == VerticalAlignment.SUPERSCRIPT) {
                    nodes = list(Html.collapsibleElement("sup", nodes));
                }
                if (run.isItalic()) {
                    nodes = styleMap.getItalic().orElse(HtmlPath.collapsibleElement("em")).wrap(nodes);
                }
                if (run.isBold()) {
                    nodes = styleMap.getBold().orElse(HtmlPath.collapsibleElement("strong")).wrap(nodes);
                }
                HtmlPath mapping = styleMap.getRunHtmlPath(run)
                    .orElseGet(() -> {
                        if (run.getStyle().isPresent()) {
                            warnings.add("Unrecognised run style: " + run.getStyle().get().describe());
                        }
                        return HtmlPath.EMPTY;
                    });
                return mapping.wrap(nodes);
            }

            @Override
            public ConversionResult visit(Text text) {
                if (text.getValue().isEmpty()) {
                    return ConversionResult.EMPTY_SUCCESS;
                } else {
                    return ConversionResult.success(Html.text(text.getValue()));
                }
            }

            @Override
            public ConversionResult visit(Tab tab) {
                return ConversionResult.success(Html.text("\t"));
            }

            @Override
            public ConversionResult visit(LineBreak lineBreak) {
                return ConversionResult.success(Html.selfClosingElement("br"));
            }

            @Override
            public ConversionResult visit(Table table) {
                return convertChildrenToHtml(table)
                    .map(children -> list(Html.element("table", children)));
            }

            @Override
            public ConversionResult visit(TableRow tableRow) {
                return convertChildrenToHtml(tableRow)
                    .map(children -> list(Html.element("tr", children)));
            }

            @Override
            public ConversionResult visit(TableCell tableCell) {
                return convertChildrenToHtml(tableCell)
                    .map(children -> list(Html.element("td",
                        Lists.cons(Html.FORCE_WRITE, children))));
            }

            @Override
            public ConversionResult visit(Hyperlink hyperlink) {
                Map<String, String> attributes = map("href", generateHref(hyperlink));
                return convertChildrenToHtml(hyperlink)
                    .map(children -> list(Html.collapsibleElement("a", attributes, children)));
            }

            private String generateHref(Hyperlink hyperlink) {
                if (hyperlink.getHref().isPresent()) {
                    return hyperlink.getHref().get();
                } else if (hyperlink.getAnchor().isPresent()) {
                    return "#" + generateId(hyperlink.getAnchor().get());
                } else {
                    return "";
                }
            }

            @Override
            public ConversionResult visit(Bookmark bookmark) {
                HtmlNode element = Html.element("a", map("id", generateId(bookmark.getName())), list(Html.FORCE_WRITE));
                return ConversionResult.success(element);
            }

            @Override
            public ConversionResult visit(NoteReference noteReference) {
                noteReferences.add(noteReference);
                String noteAnchor = generateNoteHtmlId(noteReference.getNoteType(), noteReference.getNoteId());
                String noteReferenceAnchor = generateNoteRefHtmlId(noteReference.getNoteType(), noteReference.getNoteId());
                return list(Html.element("sup", list(
                    Html.element("a", map("href", "#" + noteAnchor, "id", noteReferenceAnchor), list(
                        Html.text("[" + noteReferences.size() + "]"))))));
            }

            @Override
            public ConversionResult visit(Image image) {
                // TODO: custom image handlers
                // TODO: handle empty content type
                return image.getContentType()
                    .map(contentType -> {
                        try {
                            Map<String, String> attributes = new HashMap<>();

                            String base64 = Base64.getEncoder().encodeToString(toByteArray(image.open()));
                            String src = "data:" + contentType + ";base64," + base64;
                            attributes.put("src", src);

                            image.getAltText().ifPresent(altText -> attributes.put("alt", altText));

                            return ConversionResult.success(Html.selfClosingElement("img", attributes));
                        } catch (IOException exception) {
                            warnings.add(exception.getMessage());
                            return Lists.<HtmlNode>list();
                        }
                    })
                    .orElse(ConversionResult.EMPTY_SUCCESS);
            }
        });
    }

    private String generateNoteHtmlId(NoteType noteType, String noteId) {
        return generateId(noteTypeToIdFragment(noteType) + "-" + noteId);
    }

    private String generateNoteRefHtmlId(NoteType noteType, String noteId) {
        return generateId(noteTypeToIdFragment(noteType) + "-ref-" + noteId);
    }

    private String noteTypeToIdFragment(NoteType noteType) {
        switch (noteType) {
            case FOOTNOTE:
                return "footnote";
            case ENDNOTE:
                return "endnote";
            default:
                throw new UnsupportedOperationException();
        }
    }

    private String generateId(String bookmarkName) {
        return idPrefix + bookmarkName;
    }
}
