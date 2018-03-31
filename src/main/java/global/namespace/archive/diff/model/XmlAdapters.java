/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Collection;
import java.util.Map;

final class EntryNameAndDigestMapAdapter
        extends XmlAdapter<EntryNameAndDigestCollectionDto, Map<String, EntryNameAndDigestValue>> {

    @Override
    public Map<String, EntryNameAndDigestValue> unmarshal(EntryNameAndDigestCollectionDto dto) {
        return null == dto ? null : DeltaModel.unchangedMap(dto.entries);
    }

    @Override
    public EntryNameAndDigestCollectionDto marshal(final Map<String, EntryNameAndDigestValue> map) {
        if (null == map || map.isEmpty()) {
            return null;
        }
        final EntryNameAndDigestCollectionDto dto = new EntryNameAndDigestCollectionDto();
        dto.entries = map.values();
        return dto;
    }
}

final class EntryNameAndDigestCollectionDto {

    @XmlElement(name = "entry")
    public Collection<EntryNameAndDigestValue> entries;
}

final class EntryNameAndTwoDigestsMapAdapter
        extends XmlAdapter<EntryNameAndTwoDigestsCollectionDto, Map<String, EntryNameAndTwoDigestValues>> {

    @Override
    public Map<String, EntryNameAndTwoDigestValues> unmarshal(EntryNameAndTwoDigestsCollectionDto dto) {
        return null == dto ? null : DeltaModel.changedMap(dto.entries);
    }

    @Override
    public EntryNameAndTwoDigestsCollectionDto marshal(final Map<String, EntryNameAndTwoDigestValues> map) {
        if (null == map || map.isEmpty()) {
            return null;
        }
        final EntryNameAndTwoDigestsCollectionDto dto = new EntryNameAndTwoDigestsCollectionDto();
        dto.entries = map.values();
        return dto;
    }
}

final class EntryNameAndTwoDigestsCollectionDto {

    @XmlElement(name = "entry")
    public Collection<EntryNameAndTwoDigestValues> entries;
}
