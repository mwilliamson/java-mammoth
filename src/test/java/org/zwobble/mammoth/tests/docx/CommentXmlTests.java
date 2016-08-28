package org.zwobble.mammoth.tests.docx;

import org.junit.Test;
import org.zwobble.mammoth.internal.documents.Comment;
import org.zwobble.mammoth.internal.docx.CommentXmlReader;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.xml.XmlNode;

import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.internal.xml.XmlNodes.element;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.tests.ResultMatchers.isInternalSuccess;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.PARAGRAPH;
import static org.zwobble.mammoth.tests.docx.BodyXmlReaderMakers.bodyReader;

public class CommentXmlTests {
    @Test
    public void idAndBodyOfCommentIsRead() {
        List<XmlNode> body = list(element("w:p"));
        CommentXmlReader reader = new CommentXmlReader(make(a(bodyReader)));
        InternalResult<List<Comment>> result = reader.readElement(element("w:comments", list(
            element("w:comment", map("w:id", "1"), body)
        )));
        assertThat(
            result,
            isInternalSuccess(contains(
                deepEquals(new Comment("1", list(make(a(PARAGRAPH)))))
            ))
        );
    }
}
