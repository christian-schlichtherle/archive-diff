package global.namespace.archive.diff.model.dto;

import global.namespace.archive.diff.model.EntryNameAndTwoDigestValues;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Collection;

import static java.util.Collections.emptyList;

public final class EntryNameAndTwoDigestValuesCollectionAdapter
        extends XmlAdapter<EntryNameAndTwoDigestValuesCollectionDTO, Collection<EntryNameAndTwoDigestValues>> {

    @Override
    public Collection<EntryNameAndTwoDigestValues> unmarshal(EntryNameAndTwoDigestValuesCollectionDTO v) {
        return null == v ? emptyList() : v.entries;
    }

    @Override
    public EntryNameAndTwoDigestValuesCollectionDTO marshal(final Collection<EntryNameAndTwoDigestValues> v) {
        if (null == v || v.isEmpty()) {
            return null;
        } else {
            final EntryNameAndTwoDigestValuesCollectionDTO dto = new EntryNameAndTwoDigestValuesCollectionDTO();
            dto.entries = v;
            return dto;
        }
    }
}
