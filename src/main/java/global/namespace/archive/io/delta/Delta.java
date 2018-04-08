/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.io.delta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import global.namespace.archive.io.api.ArchiveFileInput;
import global.namespace.archive.io.api.ArchiveFileOutput;
import global.namespace.archive.io.delta.dto.DeltaDTO;
import global.namespace.archive.io.delta.dto.EntryNameAndDigestValueDTO;
import global.namespace.archive.io.delta.dto.EntryNameAndTwoDigestValuesDTO;
import global.namespace.archive.io.delta.model.DeltaModel;
import global.namespace.archive.io.delta.model.EntryNameAndDigestValue;
import global.namespace.archive.io.delta.model.EntryNameAndTwoDigestValues;
import global.namespace.fun.io.api.Codec;
import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Source;
import global.namespace.fun.io.jackson.Jackson;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * Diffs and patches archive files.
 *
 * @author Christian Schlichtherle
 */
public class Delta {

    private Delta() { }

    private static final String META_INF_DELTA_JSON = "META-INF/delta.json";

    /**
     * Returns a builder for comparing a first archive file to a second archive file and generating a delta archive
     * file.
     */
    public static ArchiveFileDiffBuilder diff() { return new ArchiveFileDiffBuilder(); }

    /** Returns a builder for patching the first archive file to a second archive file using a delta archive file. */
    public static ArchiveFilePatchBuilder patch() { return new ArchiveFilePatchBuilder(); }

    static <E> void encodeModel(ArchiveFileOutput<E> output, DeltaModel model) throws Exception {
        encodeModel(output.sink(META_INF_DELTA_JSON), model);
    }

    static <E> DeltaModel decodeModel(ArchiveFileInput<E> input) throws Exception {
        return decodeModel(input.source(META_INF_DELTA_JSON).orElseThrow(() ->
                new InvalidDeltaArchiveFileException(new MissingArchiveEntryException(META_INF_DELTA_JSON))));
    }

    /** Encodes the given delta model to the given sink. */
    static void encodeModel(Sink sink, DeltaModel model) throws Exception { encodeDTO(sink, marshal(model)); }

    /** Decodes a delta model from the given source. */
    static DeltaModel decodeModel(Source source) throws Exception { return unmarshal(decodeDTO(source)); }

    private static void encodeDTO(Sink sink, DeltaDTO dto) throws Exception { jsonCodec().encoder(sink).encode(dto); }

    private static DeltaDTO decodeDTO(Source source) throws Exception {
        return jsonCodec().decoder(source).decode(DeltaDTO.class);
    }

    private static Codec jsonCodec() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        return Jackson.jsonCodec(mapper);
    }

    private static DeltaDTO marshal(final DeltaModel v) {
        if (null == v) {
            return null;
        } else {
            final DeltaDTO dto = new DeltaDTO();
            dto.algorithm = v.digestAlgorithmName();
            dto.numBytes = v.digestByteLength().orElse(0);
            dto.changed = marshal2(v.changedEntries());
            dto.unchanged = marshal(v.unchangedEntries());
            dto.added = marshal(v.addedEntries());
            dto.removed = marshal(v.removedEntries());
            return dto;
        }
    }

    private static DeltaModel unmarshal(final DeltaDTO v) throws Exception {
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
