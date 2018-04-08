/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.api;

import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Source;

public interface ArchiveEntrySink<E> extends Sink {

    /** Returns the name of the underlying archive entry. */
    String name();

    /** Returns the underlying archive entry. */
    E entry();

    /** Returns a sink for copying the underlying archive entry from the given source to this sink. */
    default Sink prepareForCopyFrom(ArchiveEntrySource<?> source) { return this; }

    /** Returns an archive file channel for copying the underlying archive entry from the given source to this sink. */
    default ArchiveFileChannel connect(ArchiveEntrySource<?> source) {
        return new ArchiveFileChannel() {

            public Source source() { return source.prepareForCopyTo(ArchiveEntrySink.this); }

            public Sink sink() { return ArchiveEntrySink.this.prepareForCopyFrom(source); }
        };
    }
}
