package global.namespace.archive.diff.model.dto;

import global.namespace.archive.diff.model.EntryNameAndDigestValue;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Collection;

import static java.util.Collections.emptyList;

public final class EntryNameAndDigestValueCollectionAdapter
        extends XmlAdapter<EntryNameAndDigestValueCollectionDTO, Collection<EntryNameAndDigestValue>> {

    @Override
    public Collection<EntryNameAndDigestValue> unmarshal(EntryNameAndDigestValueCollectionDTO v) {
        return null == v ? emptyList() : v.entries;
    }

    @Override
    public EntryNameAndDigestValueCollectionDTO marshal(final Collection<EntryNameAndDigestValue> v) {
        if (null == v || v.isEmpty()) {
            return null;
        } else {
            final EntryNameAndDigestValueCollectionDTO dto = new EntryNameAndDigestValueCollectionDTO();
            dto.entries = v;
            return dto;
        }
    }
}
