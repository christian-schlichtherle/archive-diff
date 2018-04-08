/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.api;

import global.namespace.fun.io.api.Socket;
import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.Closeable;
import java.io.OutputStream;

/**
 * An abstraction for writing archive entries to an archive file.
 *
 * @see ArchiveFileInput
 * @author Christian Schlichtherle
 */
public interface ArchiveFileOutput<E> extends Closeable {

    /** Returns a <em>new</em> archive entry. */
    ArchiveFileEntry<E> entry(String name);

    /** Returns an output stream socket for writing the content of the given archive entry. */
    Socket<OutputStream> output(ArchiveFileEntry<E> entry);

    /** Returns a sink for writing the content of the given archive entry. */
    default ArchiveEntrySink<E> sink(ArchiveFileEntry<E> entry) {
        return new ArchiveEntrySink<E>() {

            public String name() { return entry.name(); }

            public E entry() { return entry.entry(); }

            public Socket<OutputStream> output() { return ArchiveFileOutput.this.output(entry); }
        };
    }
}
