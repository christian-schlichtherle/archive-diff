package global.namespace.archive.diff.model.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "delta")
public class DeltaModelDTO {

    @XmlAttribute(required = true)
    public String algorithm;

    @XmlAttribute
    public Integer numBytes;

    public EntryNameAndTwoDigestValuesCollectionDTO changed;

    public EntryNameAndDigestValueCollectionDTO unchanged, added, removed;
}
