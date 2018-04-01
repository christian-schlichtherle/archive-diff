package global.namespace.archive.diff.model.dto;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

public class EntryNameAndTwoDigestValuesDTO implements Serializable {

    @XmlAttribute(required = true)
    public String name, first, second;
}
