package global.namespace.archive.diff.model.dto;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

public class EntryNameAndDigestValueDTO implements Serializable {

    @XmlAttribute(required = true)
    public String name, digest;
}
