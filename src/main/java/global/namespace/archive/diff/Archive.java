/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import global.namespace.archive.api.ArchiveFileInput;
import global.namespace.archive.api.ArchiveFileOutput;
import global.namespace.archive.delta.Delta;
import global.namespace.archive.delta.model.DeltaModel;

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
        Delta.encodeModel(output.sink(ENTRY_NAME), model);
    }

    static <E> DeltaModel decodeModel(ArchiveFileInput<E> input) throws Exception {
        return Delta.decodeModel(input.source(ENTRY_NAME).orElseThrow(() ->
                new InvalidDeltaArchiveFileException(new MissingArchiveEntryException(ENTRY_NAME))));
    }
}
