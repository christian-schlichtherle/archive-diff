/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.io.api;

import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Socket;
import global.namespace.fun.io.api.Source;

import java.io.OutputStream;

/**
 * An abstraction for writing the content of an underlying archive entry.
 *
 * @author Christian Schlichtherle
 */
public abstract class ArchiveEntrySink<E> extends ArchiveEntry<E> implements Sink {

    /**
     * Returns an output stream socket for copying the underlying archive entry in this archive file from the given
     * archive entry source.
     */
    public abstract Socket<OutputStream> output(ArchiveEntrySource<?> source);

    /**
     * Returns an archive file channel for copying the underlying archive entry in this archive file from the given
     * archive entry source.
     */
    public ArchiveEntryChannel connect(ArchiveEntrySource<?> source) {
        return new ArchiveEntryChannel() {

            public Source source() { return () -> source.input(ArchiveEntrySink.this); }

            public Sink sink() { return () -> ArchiveEntrySink.this.output(source); }
        };
    }
}
