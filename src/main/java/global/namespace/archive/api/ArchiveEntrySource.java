/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.api;

import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Source;

public interface ArchiveEntrySource<E> extends Source {

    /** Returns the name of the underlying archive entry. */
    String name();

    /** Returns the underlying archive entry. */
    E entry();

    /** Returns a source for copying the underlying archive entry from this source to the given sink. */
    default Source prepareForCopyTo(ArchiveEntrySink<?> sink) { return this; }

    /** Returns an archive file channel for copying the underlying archive entry from this source to the given sink. */
    default ArchiveFileChannel connect(ArchiveEntrySink<?> sink) {
        return new ArchiveFileChannel() {

            public Source source() { return ArchiveEntrySource.this.prepareForCopyTo(sink); }

            public Sink sink() { return sink.prepareForCopyFrom(ArchiveEntrySource.this); }
        };
    }
}
