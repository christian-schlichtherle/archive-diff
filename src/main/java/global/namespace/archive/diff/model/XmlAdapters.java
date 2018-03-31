/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

final class EntryNameAndDigestValueMapAdapter
        extends XmlAdapter<EntryNameAndDigestValueCollectionDto, Map<String, EntryNameAndDigestValue>> {

    @Override
    public Map<String, EntryNameAndDigestValue> unmarshal(EntryNameAndDigestValueCollectionDto dto) {
        return null == dto ? null : DeltaModel.unchangedMap(dto.entries);
    }

    @Override
    public EntryNameAndDigestValueCollectionDto marshal(final Map<String, EntryNameAndDigestValue> map) {
        if (null == map || map.isEmpty()) {
            return null;
        }
        final EntryNameAndDigestValueCollectionDto dto = new EntryNameAndDigestValueCollectionDto();
        dto.entries = map.values();
        return dto;
    }
}

final class EntryNameAndDigestValueCollectionDto {

    @XmlElement(name = "entry")
    public Collection<EntryNameAndDigestValue> entries;
}

final class EntryNameAndTwoDigestValuesMapAdapter
        extends XmlAdapter<EntryNameAndTwoDigestValuesCollectionDto, Map<String, EntryNameAndTwoDigestValues>> {

    @Override
    public Map<String, EntryNameAndTwoDigestValues> unmarshal(EntryNameAndTwoDigestValuesCollectionDto dto) {
        return null == dto ? null : DeltaModel.changedMap(dto.entries);
    }

    @Override
    public EntryNameAndTwoDigestValuesCollectionDto marshal(final Map<String, EntryNameAndTwoDigestValues> map) {
        if (null == map || map.isEmpty()) {
            return null;
        }
        final EntryNameAndTwoDigestValuesCollectionDto dto = new EntryNameAndTwoDigestValuesCollectionDto();
        dto.entries = map.values();
        return dto;
    }
}

final class EntryNameAndTwoDigestValuesCollectionDto {

    @XmlElement(name = "entry")
    public Collection<EntryNameAndTwoDigestValues> entries;
}

final class OptionalIntegerAdapter extends XmlAdapter<Integer, Optional<Integer>> {

    @Override
    public Optional<Integer> unmarshal(Integer v) throws Exception {
        return Optional.ofNullable(v);
    }

    @Override
    public Integer marshal(Optional<Integer> v) throws Exception {
        return v.orElse(null);
    }
}
