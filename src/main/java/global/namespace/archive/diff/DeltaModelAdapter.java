/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import global.namespace.archive.diff.dto.*;
import global.namespace.archive.diff.model.DeltaModel;
import global.namespace.archive.diff.model.EntryNameAndDigestValue;
import global.namespace.archive.diff.model.EntryNameAndTwoDigestValues;

import java.security.MessageDigest;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

class DeltaModelAdapter {

    private DeltaModelAdapter() { }

    static DeltaModelDTO marshal(final DeltaModel v) {
        if (null == v) {
            return null;
        } else {
            final DeltaModelDTO dto = new DeltaModelDTO();
            dto.algorithm = v.digestAlgorithmName();
            dto.numBytes = v.digestByteLength().orElse(null);
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

    private static EntryNameAndTwoDigestValuesListDTO marshal2(final Collection<EntryNameAndTwoDigestValues> v) {
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

    private static Collection<EntryNameAndTwoDigestValues> unmarshal2(EntryNameAndTwoDigestValuesListDTO v) {
        return null == v ? emptyList() : v.entries
                .stream()
                .map(dto -> new EntryNameAndTwoDigestValues(dto.name, dto.first, dto.second))
                .collect(Collectors.toList());
    }

    private static EntryNameAndDigestValueListDTO marshal(final Collection<EntryNameAndDigestValue> v) {
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

    private static Collection<EntryNameAndDigestValue> unmarshal(EntryNameAndDigestValueListDTO v) {
        return null == v ? emptyList() : v.entries
                .stream()
                .map(dto -> new EntryNameAndDigestValue(dto.name, dto.digest))
                .collect(Collectors.toList());
    }
}
