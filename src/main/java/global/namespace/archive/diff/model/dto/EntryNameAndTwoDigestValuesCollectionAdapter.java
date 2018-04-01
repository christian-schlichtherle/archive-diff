package global.namespace.archive.diff.model.dto;

import global.namespace.archive.diff.model.EntryNameAndTwoDigestValues;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public final class EntryNameAndTwoDigestValuesCollectionAdapter
        extends XmlAdapter<EntryNameAndTwoDigestValuesListDTO, Collection<EntryNameAndTwoDigestValues>> {

    @Override
    public Collection<EntryNameAndTwoDigestValues> unmarshal(EntryNameAndTwoDigestValuesListDTO v) {
        return null == v ? emptyList() : v.entries
                .stream()
                .map(dto -> new EntryNameAndTwoDigestValues(dto.name, dto.first, dto.second))
                .collect(Collectors.toList());
    }

    @Override
    public EntryNameAndTwoDigestValuesListDTO marshal(final Collection<EntryNameAndTwoDigestValues> v) {
        if (null == v || v.isEmpty()) {
            return null;
        } else {
            final EntryNameAndTwoDigestValuesListDTO listDTO = new EntryNameAndTwoDigestValuesListDTO();
            listDTO.entries = v.stream().map(entry -> {
                final EntryNameAndTwoDigestValuesDTO dto = new EntryNameAndTwoDigestValuesDTO();
                dto.name = entry.entryName();
                dto.first = entry.firstDigestValue();
                dto.second = entry.secondDigestValue();
                return dto;
            }).collect(Collectors.toList());
            return listDTO;
        }
    }
}
