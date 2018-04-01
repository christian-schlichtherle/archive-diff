package global.namespace.archive.diff.model.dto;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

public class EntryNameAndDigestValueListDTO implements Serializable {

    @XmlElement(name = "entry")
    public List<EntryNameAndDigestValueDTO> entries;
}
