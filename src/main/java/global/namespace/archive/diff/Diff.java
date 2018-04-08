/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import global.namespace.archive.api.ArchiveFileOutput;
import global.namespace.archive.delta.Delta;
import global.namespace.archive.delta.model.DeltaModel;

/**
 * Diffs archive files.
 *
 * @author Christian Schlichtherle
 */
public class Diff {

    private Diff() { }

    /**
     * Returns a builder for comparing a first archive file to a second archive file and generating a delta archive
     * file.
     */
    public static ArchiveFileDiffBuilder diff() { return new ArchiveFileDiffBuilder(); }

    static <E> void encodeModel(ArchiveFileOutput<E> output, DeltaModel model) throws Exception {
        Delta.encodeModel(output.sink("META-INF/delta.json"), model);
    }
}
