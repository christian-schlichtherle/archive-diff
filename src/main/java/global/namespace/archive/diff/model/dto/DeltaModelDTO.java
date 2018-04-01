package global.namespace.archive.diff.model.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "delta")
public class DeltaModelDTO implements Serializable {

    @XmlAttribute(required = true)
    public String algorithm;

    @XmlAttribute
    public Integer numBytes;

    public EntryNameAndTwoDigestValuesListDTO changed;

    public EntryNameAndDigestValueListDTO unchanged, added, removed;
}
