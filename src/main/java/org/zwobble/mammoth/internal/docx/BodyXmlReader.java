package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.archives.Archive;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlNode;

public class BodyXmlReader {
    private final Styles styles;
    private final Numbering numbering;
    private final Relationships relationships;
    private final ContentTypes contentTypes;
    private final Archive file;
    private final FileReader fileReader;

    public BodyXmlReader(
        Styles styles,
        Numbering numbering,
        Relationships relationships,
        ContentTypes contentTypes,
        Archive file,
        FileReader fileReader
    )
    {
        this.styles = styles;
        this.numbering = numbering;
        this.relationships = relationships;
        this.contentTypes = contentTypes;
        this.file = file;
        this.fileReader = fileReader;
    }

    public ReadResult readElements(Iterable<XmlNode> nodes) {
        return new StatefulBodyXmlReader(
            styles,
            numbering,
            relationships,
            contentTypes,
            file,
            fileReader
        ).readElements(nodes);
    }

    public ReadResult readElement(XmlElement element) {
        return new StatefulBodyXmlReader(
            styles,
            numbering,
            relationships,
            contentTypes,
            file,
            fileReader
        ).readElement(element);
    }
}
