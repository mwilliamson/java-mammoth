package org.zwobble.mammoth;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import org.zwobble.mammoth.documents.*;
import org.zwobble.mammoth.html.Html;
import org.zwobble.mammoth.html.HtmlNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.zwobble.mammoth.util.MammothLists.eagerFlatMap;
import static org.zwobble.mammoth.util.MammothLists.list;
import static org.zwobble.mammoth.util.MammothMaps.map;

public class DocumentConverter {
    public static List<HtmlNode> convertToHtml(String idPrefix, Document document) {
        DocumentConverter documentConverter = new DocumentConverter(idPrefix);
        List<HtmlNode> mainBody = documentConverter.convertChildrenToHtml(document);
        // TODO: can you have note references inside a note?
        List<Note> notes = findNotes(document, documentConverter.noteReferences);
        if (notes.isEmpty()) {
            return mainBody;
        } else {
            HtmlNode noteNode = Html.element("ol",
                ImmutableList.copyOf(Iterables.transform(notes, documentConverter::convertToHtml)));

            return ImmutableList.copyOf(Iterables.concat(mainBody, list(noteNode)));
        }
    }

    private static List<Note> findNotes(Document document, Iterable<NoteReference> noteReferences) {
        return ImmutableList.copyOf(Iterables.transform(
            noteReferences,
            // TODO: handle missing notes
            reference -> document.getNotes().findNote(reference.getNoteType(), reference.getNoteId()).get()));
    }

    public static List<HtmlNode> convertToHtml(String idPrefix, DocumentElement element) {
        return new DocumentConverter(idPrefix).convertToHtml(element);
    }

    private final String idPrefix;
    private final List<NoteReference> noteReferences = new ArrayList<>();

    private DocumentConverter(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    private HtmlNode convertToHtml(Note note) {
        String id = generateNoteHtmlId(note.getNoteType(), note.getId());
        String referenceId = generateNoteRefHtmlId(note.getNoteType(), note.getId());
        ImmutableList.Builder<HtmlNode> children = ImmutableList.builder();
        children.addAll(convertToHtml(note.getBody()));
        // TODO: should be collapsible
        children.add(Html.element("p", list(
            Html.text(" "),
            Html.element("a", map("href", "#" + referenceId), list(Html.text("↑"))))));
        return Html.element("li", map("id", id), children.build());
    }

    private List<HtmlNode> convertToHtml(List<DocumentElement> elements) {
        return eagerFlatMap(
            elements,
            this::convertToHtml);
    }

    private List<HtmlNode> convertChildrenToHtml(HasChildren element) {
        return convertToHtml(element.getChildren());
    }

    private List<HtmlNode> convertToHtml(DocumentElement element) {
        return element.accept(new DocumentElementVisitor<List<HtmlNode>>() {
            @Override
            public List<HtmlNode> visit(Paragraph paragraph) {
                List<HtmlNode> content = convertChildrenToHtml(paragraph);
                if (content.isEmpty()) {
                    return list();
                } else {
                    return list(Html.element("p", content));
                }
            }

            @Override
            public List<HtmlNode> visit(Run run) {
                List<HtmlNode> nodes = convertChildrenToHtml(run);
                if (run.isStrikethrough()) {
                    nodes = list(Html.element("s", nodes));
                }
                if (run.getVerticalAlignment() == VerticalAlignment.SUBSCRIPT) {
                    nodes = list(Html.element("sub", nodes));
                }
                if (run.getVerticalAlignment() == VerticalAlignment.SUPERSCRIPT) {
                    nodes = list(Html.element("sup", nodes));
                }
                if (run.isItalic()) {
                    nodes = list(Html.element("em", nodes));
                }
                if (run.isBold()) {
                    nodes = list(Html.element("strong", nodes));
                }
                return nodes;
            }

            @Override
            public List<HtmlNode> visit(Text text) {
                if (text.getValue().isEmpty()) {
                    return list();
                } else {
                    return list(Html.text(text.getValue()));
                }
            }

            @Override
            public List<HtmlNode> visit(Tab tab) {
                return list(Html.text("\t"));
            }

            @Override
            public List<HtmlNode> visit(LineBreak lineBreak) {
                return list(Html.selfClosingElement("br"));
            }

            @Override
            public List<HtmlNode> visit(Table table) {
                return list(Html.element("table", convertChildrenToHtml(table)));
            }

            @Override
            public List<HtmlNode> visit(TableRow tableRow) {
                return list(Html.element("tr", convertChildrenToHtml(tableRow)));
            }

            @Override
            public List<HtmlNode> visit(TableCell tableCell) {
                return list(Html.element("td", convertChildrenToHtml(tableCell)));
            }

            @Override
            public List<HtmlNode> visit(Hyperlink hyperlink) {
                Map<String, String> attributes = map("href", generateHref(hyperlink));
                return list(Html.element("a", attributes, convertChildrenToHtml(hyperlink)));
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
            public List<HtmlNode> visit(Bookmark bookmark) {
                return list(Html.element("a", map("id", generateId(bookmark.getName()))));
            }

            @Override
            public List<HtmlNode> visit(NoteReference noteReference) {
                noteReferences.add(noteReference);
                String noteAnchor = generateNoteHtmlId(noteReference.getNoteType(), noteReference.getNoteId());
                String noteReferenceAnchor = generateNoteRefHtmlId(noteReference.getNoteType(), noteReference.getNoteId());
                return list(Html.element("sup", list(
                    Html.element("a", map("href", "#" + noteAnchor, "id", noteReferenceAnchor), list(
                        Html.text("[" + noteReferences.size() + "]"))))));
            }

            @Override
            public List<HtmlNode> visit(Image image) {
                // TODO: custom image handlers
                // TODO: handle empty content type
                return image.getContentType()
                    .map(contentType -> {
                        try {
                            ImmutableMap.Builder<String, String> attributes = ImmutableMap.builder();

                            String base64 = Base64.getEncoder().encodeToString(ByteStreams.toByteArray(image.open()));
                            String src = "data:" + contentType + ";base64," + base64;
                            attributes.put("src", src);

                            image.getAltText().ifPresent(altText -> attributes.put("alt", altText));

                            return list(Html.selfClosingElement("img", attributes.build()));
                        } catch (IOException exception) {
                            // TODO: return a result with a warning
                            throw new RuntimeException(exception);
                        }
                    })
                    .orElse(list());
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
        return idPrefix + "-" + bookmarkName;
    }
}
