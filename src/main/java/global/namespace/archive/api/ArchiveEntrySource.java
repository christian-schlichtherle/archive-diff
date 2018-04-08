/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.api;

import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Socket;
import global.namespace.fun.io.api.Source;

import java.io.InputStream;

/**
 * An abstraction for reading the content of an underlying archive entry.
 *
 * @author Christian Schlichtherle
 */
public abstract class ArchiveEntrySource<E> extends ArchiveEntry<E> implements Source {

    /**
     * Returns an input stream socket for copying the underlying archive entry in this archive file to the given
     * archive entry sink.
     */
    public abstract Socket<InputStream> input(ArchiveEntrySink<?> sink);

    /**
     * Returns an archive file channel for copying the underlying archive entry in this archive file to the given
     * archive entry sink.
     */
    public ArchiveEntryChannel connect(ArchiveEntrySink<?> sink) {
        return new ArchiveEntryChannel() {

            public Source source() { return () -> ArchiveEntrySource.this.input(sink); }

            public Sink sink() { return () -> sink.output(ArchiveEntrySource.this); }
        };
    }
}
