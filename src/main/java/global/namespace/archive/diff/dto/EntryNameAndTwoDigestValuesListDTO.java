package global.namespace.archive.diff.dto;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

public final class EntryNameAndTwoDigestValuesListDTO implements Serializable {

    @XmlElement(name = "entry")
    public List<EntryNameAndTwoDigestValuesDTO> entries;
}
