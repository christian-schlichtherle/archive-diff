package global.namespace.archive.diff.dto;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

public final class EntryNameAndTwoDigestValuesDTO implements Serializable {

    @XmlAttribute(required = true)
    public String name, first, second;
}
