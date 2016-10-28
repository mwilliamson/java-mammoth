package org.zwobble.mammoth.internal.conversion;

import org.zwobble.mammoth.internal.documents.*;
import org.zwobble.mammoth.internal.html.Html;
import org.zwobble.mammoth.internal.html.HtmlNode;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.styles.HtmlPath;
import org.zwobble.mammoth.internal.styles.StyleMap;
import org.zwobble.mammoth.internal.util.Base64Encoding;
import org.zwobble.mammoth.internal.util.Lists;
import org.zwobble.mammoth.internal.util.Maps;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

import static org.zwobble.mammoth.internal.util.Lists.*;
import static org.zwobble.mammoth.internal.util.Maps.lookup;
import static org.zwobble.mammoth.internal.util.Maps.map;

public class DocumentToHtml {
    public static InternalResult<List<HtmlNode>> convertToHtml(Document document, DocumentToHtmlOptions options) {
        DocumentToHtml documentConverter = new DocumentToHtml(options, document.getComments());
        return new InternalResult<>(
            documentConverter.convertToHtml(document),
            documentConverter.warnings);
    }

    private static List<Note> findNotes(Document document, Iterable<NoteReference> noteReferences) {
        return eagerMap(
            noteReferences,
            // TODO: handle missing notes
            reference -> document.getNotes().findNote(reference.getNoteType(), reference.getNoteId()).get());
    }

    public static InternalResult<List<HtmlNode>> convertToHtml(DocumentElement element, DocumentToHtmlOptions options) {
        DocumentToHtml documentConverter = new DocumentToHtml(options, list());
        return new InternalResult<>(
            documentConverter.convertToHtml(element),
            documentConverter.warnings);
    }

    private static class ReferencedComment {
        private final String label;
        private final Comment comment;

        private ReferencedComment(String label, Comment comment) {
            this.label = label;
            this.comment = comment;
        }
    }

    private final String idPrefix;
    private final boolean preserveEmptyParagraphs;
    private final StyleMap styleMap;
    private final Map<String, Comment> comments;
    private final List<NoteReference> noteReferences = new ArrayList<>();
    private final List<ReferencedComment> referencedComments = new ArrayList<>();
    private final Set<String> warnings = new HashSet<>();

    private DocumentToHtml(DocumentToHtmlOptions options, List<Comment> comments) {
        this.idPrefix = options.idPrefix();
        this.preserveEmptyParagraphs = options.shouldPreserveEmptyParagraphs();
        this.styleMap = options.styleMap();
        this.comments = Maps.toMapWithKey(comments, Comment::getCommentId);
    }

    private List<HtmlNode> convertToHtml(Document document) {
        List<HtmlNode> mainBody = convertChildrenToHtml(document);
        // TODO: can you have note references inside a note?
        List<Note> notes = findNotes(document, noteReferences);

        List<HtmlNode> noteNodes = notes.isEmpty()
            ? list()
            : list(Html.element("ol", eagerMap(notes, this::convertToHtml)));

        List<HtmlNode> commentNodes = referencedComments.isEmpty()
            ? list()
            : list(Html.element("dl", eagerFlatMap(referencedComments, this::convertToHtml)));

        return eagerConcat(mainBody, noteNodes, commentNodes);
    }

    private HtmlNode convertToHtml(Note note) {
        String id = generateNoteHtmlId(note.getNoteType(), note.getId());
        String referenceId = generateNoteRefHtmlId(note.getNoteType(), note.getId());
        List<HtmlNode> noteBody = convertToHtml(note.getBody());
        // TODO: we probably want this to collapse more eagerly than other collapsible elements
        // -- for instance, any paragraph will probably do, regardless of attributes. (Possible other elements will do too.)
        HtmlNode backLink = Html.collapsibleElement("p", list(
            Html.text(" "),
            Html.element("a", map("href", "#" + referenceId), list(Html.text("↑")))));
        return Html.element("li", map("id", id), eagerConcat(noteBody, list(backLink)));
    }

    private List<HtmlNode> convertToHtml(ReferencedComment referencedComment) {
        // TODO: remove duplication with notes
        String commentId = referencedComment.comment.getCommentId();
        List<HtmlNode> body = convertToHtml(referencedComment.comment.getBody());
        // TODO: we probably want this to collapse more eagerly than other collapsible elements
        // -- for instance, any paragraph will probably do, regardless of attributes. (Possible other elements will do too.)
        HtmlNode backLink = Html.collapsibleElement("p", list(
            Html.text(" "),
            Html.element("a", map("href", "#" + generateReferenceHtmlId("comment", commentId)), list(Html.text("↑")))));

        return list(
            Html.element(
                "dt",
                map("id", generateReferentHtmlId("comment", commentId)),
                list(Html.text("Comment " + referencedComment.label))),
            Html.element("dd",
                eagerConcat(body, list(backLink))));
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
                Supplier<List<HtmlNode>> children = () -> {
                    List<HtmlNode> content = convertChildrenToHtml(paragraph);
                    return preserveEmptyParagraphs ? cons(Html.FORCE_WRITE, content) : content;
                };
                HtmlPath mapping = styleMap.getParagraphHtmlPath(paragraph)
                    .orElseGet(() -> {
                        if (paragraph.getStyle().isPresent()) {
                            warnings.add("Unrecognised paragraph style: " + paragraph.getStyle().get().describe());
                        }
                        return HtmlPath.element("p");
                    });
                return mapping.wrap(children).get();
            }

            @Override
            public List<HtmlNode> visit(Run run) {
                Supplier<List<HtmlNode>> nodes = () -> convertChildrenToHtml(run);
                if (run.isStrikethrough()) {
                    nodes = styleMap.getStrikethrough().orElse(HtmlPath.collapsibleElement("s")).wrap(nodes);
                }
                if (run.isUnderline()) {
                    nodes = styleMap.getUnderline().orElse(HtmlPath.EMPTY).wrap(nodes);
                }
                if (run.getVerticalAlignment() == VerticalAlignment.SUBSCRIPT) {
                    nodes = HtmlPath.collapsibleElement("sub").wrap(nodes);
                }
                if (run.getVerticalAlignment() == VerticalAlignment.SUPERSCRIPT) {
                    nodes = HtmlPath.collapsibleElement("sup").wrap(nodes);
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
                return mapping.wrap(nodes).get();
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
                Map<String, String> attributes = new HashMap<>();
                if (tableCell.getColspan() != 1) {
                    attributes.put("colspan", Integer.toString(tableCell.getColspan()));
                }
                if (tableCell.getRowspan() != 1) {
                    attributes.put("rowspan", Integer.toString(tableCell.getRowspan()));
                }
                return list(Html.element("td", attributes,
                    Lists.cons(Html.FORCE_WRITE, convertChildrenToHtml(tableCell))));
            }

            @Override
            public List<HtmlNode> visit(Hyperlink hyperlink) {
                Map<String, String> attributes = map("href", generateHref(hyperlink));
                return list(Html.collapsibleElement("a", attributes, convertChildrenToHtml(hyperlink)));
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
            public List<HtmlNode> visit(BookmarkStart bookmarkStart) {
                return list(Html.element("a", map("id", generateId("bookmarkStart-" + bookmarkStart.getName())), list(Html.FORCE_WRITE)));
            }

            @Override
            public List<HtmlNode> visit(BookmarkEnd bookmarkEnd) {
                return list(Html.element("a", map("id", generateId("bookmarkEnd-" + bookmarkEnd.getName())), list(Html.FORCE_WRITE)));
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
            public List<HtmlNode> visit(CommentReference commentReference) {
                return styleMap.getCommentReference().orElse(HtmlPath.IGNORE).wrap(() -> {
                    String commentId = commentReference.getCommentId();
                    Comment comment = lookup(comments, commentId)
                        .orElseThrow(() -> new RuntimeException("Referenced comment could not be found, id: " + commentId));
                    String label = "[" + comment.getAuthorInitials().orElse("") + (referencedComments.size() + 1) + "]";
                    referencedComments.add(new ReferencedComment(label, comment));

                    // TODO: Remove duplication with note references
                    return list(Html.element(
                        "a",
                        map(
                            "href", "#" + generateReferentHtmlId("comment", commentId),
                            "id", generateReferenceHtmlId("comment", commentId)),
                        list(Html.text(label))));
                }).get();
            }

            @Override
            public List<HtmlNode> visit(Image image) {
                // TODO: custom image handlers
                // TODO: handle empty content type
                return image.getContentType()
                    .map(contentType -> {
                        try {
                            Map<String, String> attributes = new HashMap<>();

                            String base64 = Base64Encoding.streamToBase64(image::open);
                            String src = "data:" + contentType + ";base64," + base64;
                            attributes.put("src", src);

                            image.getAltText().ifPresent(altText -> attributes.put("alt", altText));

                            return list(Html.selfClosingElement("img", attributes));
                        } catch (IOException exception) {
                            warnings.add(exception.getMessage());
                            return Lists.<HtmlNode>list();
                        }
                    })
                    .orElse(list());
            }
        });
    }

    private String generateNoteHtmlId(NoteType noteType, String noteId) {
        return generateReferentHtmlId(noteTypeToIdFragment(noteType), noteId);
    }

    private String generateNoteRefHtmlId(NoteType noteType, String noteId) {
        return generateReferenceHtmlId(noteTypeToIdFragment(noteType), noteId);
    }

    private String generateReferentHtmlId(String referenceType, String referenceId) {
        return generateId(referenceType + "-" + referenceId);
    }

    private String generateReferenceHtmlId(String referenceType, String referenceId) {
        return generateId(referenceType + "-ref-" + referenceId);
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
