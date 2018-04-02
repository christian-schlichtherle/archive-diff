package global.namespace.archive.diff.dto;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

public final class EntryNameAndDigestValueDTO implements Serializable {

    @XmlAttribute(required = true)
    public String name, digest;
}
