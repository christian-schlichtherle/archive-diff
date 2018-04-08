/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.patch;

import global.namespace.archive.api.ArchiveFileInput;
import global.namespace.archive.delta.Delta;
import global.namespace.archive.delta.model.DeltaModel;

/**
 * Patches archive files.
 *
 * @author Christian Schlichtherle
 */
public class Patch {

    private Patch() { }

    /** Returns a builder for patching the first archive file to a second archive file using a delta archive file. */
    public static ArchiveFilePatchBuilder patch() { return new ArchiveFilePatchBuilder(); }

    static <E> DeltaModel decodeModel(ArchiveFileInput<E> input) throws Exception {
        return Delta.decodeModel(input.source("META-INF/delta.json").orElseThrow(() ->
                new InvalidDeltaArchiveFileException(new MissingArchiveEntryException("META-INF/delta.json"))));
    }
}
