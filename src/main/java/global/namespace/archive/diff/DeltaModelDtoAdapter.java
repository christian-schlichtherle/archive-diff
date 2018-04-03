/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import global.namespace.archive.diff.dto.DeltaModelDTO;
import global.namespace.archive.diff.dto.EntryNameAndDigestValueDTO;
import global.namespace.archive.diff.dto.EntryNameAndTwoDigestValuesDTO;
import global.namespace.archive.diff.model.DeltaModel;
import global.namespace.archive.diff.model.EntryNameAndDigestValue;
import global.namespace.archive.diff.model.EntryNameAndTwoDigestValues;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/** @author Christian Schlichtherle */
class DeltaModelDtoAdapter {

    private DeltaModelDtoAdapter() { }

    static DeltaModelDTO marshal(final DeltaModel v) {
        if (null == v) {
            return null;
        } else {
            final DeltaModelDTO dto = new DeltaModelDTO();
            dto.algorithm = v.digestAlgorithmName();
            dto.numBytes = v.digestByteLength().orElse(0);
            dto.changed = marshal2(v.changedEntries());
            dto.unchanged = marshal(v.unchangedEntries());
            dto.added = marshal(v.addedEntries());
            dto.removed = marshal(v.removedEntries());
            return dto;
        }
    }

    static DeltaModel unmarshal(final DeltaModelDTO v) throws Exception {
        if (null == v) {
            return null;
        } else {
            return DeltaModel
                    .builder()
                    .messageDigest(MessageDigest.getInstance(v.algorithm))
                    .changedEntries(unmarshal2(v.changed))
                    .unchangedEntries(unmarshal(v.unchanged))
                    .addedEntries(unmarshal(v.added))
                    .removedEntries(unmarshal(v.removed))
                    .build();
        }
    }

    private static EntryNameAndTwoDigestValuesDTO[] marshal2(final Collection<EntryNameAndTwoDigestValues> v) {
        if (null == v || v.isEmpty()) {
            return null;
        } else {
            return v.stream().map(entry -> {
                final EntryNameAndTwoDigestValuesDTO dto = new EntryNameAndTwoDigestValuesDTO();
                dto.name = entry.entryName();
                dto.first = entry.firstDigestValue();
                dto.second = entry.secondDigestValue();
                return dto;
            }).toArray(EntryNameAndTwoDigestValuesDTO[]::new);
        }
    }

    private static List<EntryNameAndTwoDigestValues> unmarshal2(EntryNameAndTwoDigestValuesDTO[] v) {
        return null == v ? emptyList() : Arrays.stream(v)
                .map(dto -> new EntryNameAndTwoDigestValues(dto.name, dto.first, dto.second))
                .collect(Collectors.toList());
    }

    private static EntryNameAndDigestValueDTO[] marshal(final Collection<EntryNameAndDigestValue> v) {
        if (null == v || v.isEmpty()) {
            return null;
        } else {
            return v.stream().map(entry -> {
                final EntryNameAndDigestValueDTO dto = new EntryNameAndDigestValueDTO();
                dto.name = entry.entryName();
                dto.digest = entry.digestValue();
                return dto;
            }).toArray(EntryNameAndDigestValueDTO[]::new);
        }
    }

    private static List<EntryNameAndDigestValue> unmarshal(EntryNameAndDigestValueDTO[] v) {
        return null == v ? emptyList() : Arrays.stream(v)
                .map(dto -> new EntryNameAndDigestValue(dto.name, dto.digest))
                .collect(Collectors.toList());
    }
}
