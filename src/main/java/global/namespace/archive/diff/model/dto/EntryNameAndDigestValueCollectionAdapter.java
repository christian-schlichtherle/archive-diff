package global.namespace.archive.diff.model.dto;

import global.namespace.archive.diff.model.EntryNameAndDigestValue;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public final class EntryNameAndDigestValueCollectionAdapter
        extends XmlAdapter<EntryNameAndDigestValueListDTO, Collection<EntryNameAndDigestValue>> {

    @Override
    public Collection<EntryNameAndDigestValue> unmarshal(EntryNameAndDigestValueListDTO v) {
        return null == v ? emptyList() : v.entries
                .stream()
                .map(dto -> new EntryNameAndDigestValue(dto.name, dto.digest))
                .collect(Collectors.toList());
    }

    @Override
    public EntryNameAndDigestValueListDTO marshal(final Collection<EntryNameAndDigestValue> v) {
        if (null == v || v.isEmpty()) {
            return null;
        } else {
            final EntryNameAndDigestValueListDTO listDTO = new EntryNameAndDigestValueListDTO();
            listDTO.entries = v.stream().map(entry -> {
                final EntryNameAndDigestValueDTO dto = new EntryNameAndDigestValueDTO();
                dto.name = entry.entryName();
                dto.digest = entry.digestValue();
                return dto;
            }).collect(Collectors.toList());
            return listDTO;
        }
    }
}
