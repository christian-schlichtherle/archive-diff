/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import global.namespace.archive.api.ArchiveFileInput;
import global.namespace.archive.api.ArchiveFileOutput;
import global.namespace.archive.delta.DeltaModelDtoAdapter;
import global.namespace.archive.delta.dto.DeltaDTO;
import global.namespace.archive.delta.model.DeltaModel;
import global.namespace.fun.io.api.Codec;
import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Source;
import global.namespace.fun.io.jackson.Jackson;

/**
 * Diffs and patches archive files.
 *
 * @author Christian Schlichtherle
 */
public class Archive {

    private Archive() { }

    /**
     * The name of the entry which contains the serialized delta model in a delta-archive file.
     * This should be the first entry in the delta-archive file.
     */
    private static final String ENTRY_NAME = "META-INF/delta.json";

    /**
     * Returns a builder for comparing a first archive file to a second archive file and generating a delta archive file.
     */
    public static ArchiveFileDiffBuilder diff() { return new ArchiveFileDiffBuilder(); }

    /** Returns a builder for patching the first archive file to a second archive file using a delta archive file. */
    public static ArchiveFilePatchBuilder patch() { return new ArchiveFilePatchBuilder(); }

    static <E> void encodeModel(ArchiveFileOutput<E> output, DeltaModel model) throws Exception {
        encodeModel(output.sink(ENTRY_NAME), model);
    }

    static void encodeModel(Sink sink, DeltaModel model) throws Exception {
        encodeDTO(sink, DeltaModelDtoAdapter.marshal(model));
    }

    private static void encodeDTO(Sink sink, DeltaDTO dto) throws Exception {
        jsonCodec().encoder(sink).encode(dto);
    }

    static <E> DeltaModel decodeModel(ArchiveFileInput<E> input) throws Exception {
        return decodeModel(input.source(ENTRY_NAME).orElseThrow(() ->
                new InvalidDeltaArchiveFileException(new MissingArchiveEntryException(ENTRY_NAME))));
    }

    static DeltaModel decodeModel(Source source) throws Exception {
        return DeltaModelDtoAdapter.unmarshal(decodeDTO(source));
    }

    private static DeltaDTO decodeDTO(Source source) throws Exception {
        return jsonCodec().decoder(source).decode(DeltaDTO.class);
    }

    private static Codec jsonCodec() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        return Jackson.jsonCodec(mapper);
    }
}
