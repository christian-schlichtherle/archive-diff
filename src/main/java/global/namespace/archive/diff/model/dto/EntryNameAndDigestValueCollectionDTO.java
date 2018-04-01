package global.namespace.archive.diff.model.dto;

import global.namespace.archive.diff.model.EntryNameAndDigestValue;

import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;

public class EntryNameAndDigestValueCollectionDTO {

    @XmlElement(name = "entry")
    public Collection<EntryNameAndDigestValue> entries;
}
