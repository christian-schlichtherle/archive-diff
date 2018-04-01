package global.namespace.archive.diff.model.dto;

import global.namespace.archive.diff.model.EntryNameAndTwoDigestValues;

import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;

public class EntryNameAndTwoDigestValuesCollectionDTO {

    @XmlElement(name = "entry")
    public Collection<EntryNameAndTwoDigestValues> entries;
}
