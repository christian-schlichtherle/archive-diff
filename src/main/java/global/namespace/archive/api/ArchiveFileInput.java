/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.api;

import global.namespace.fun.io.api.Socket;
import org.apache.commons.compress.archivers.ArchiveEntry;

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

    /** Returns an input stream for reading the contents of the given archive entry. */
    Socket<InputStream> input(ArchiveFileEntry<E> entry);
}
