/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.api;

import global.namespace.fun.io.api.Socket;
import global.namespace.fun.io.api.Source;

import java.io.Closeable;
import java.io.InputStream;
import java.util.Optional;

/**
 * An abstraction for reading archive entries from an archive file.
 *
 * @see ArchiveFileOutput
 * @author Christian Schlichtherle
 */
public interface ArchiveFileInput<E> extends Iterable<ArchiveFileEntry<E>>, Closeable {

    /** Looks up the archive entry with the given name. */
    Optional<ArchiveFileEntry<E>> entry(String name);

    /** Returns an input stream for reading the content of the given archive entry. */
    Socket<InputStream> input(ArchiveFileEntry<E> entry);

    /** Returns a source for reading the content of the given archive entry. */
    default ArchiveEntrySource<E> source(ArchiveFileEntry<E> entry) {
        return new ArchiveEntrySource<E>() {

            public String name() { return entry.name(); }

            public E entry() { return entry.entry(); }

            public Socket<InputStream> input() { return ArchiveFileInput.this.input(entry); }
        };
    }
}
