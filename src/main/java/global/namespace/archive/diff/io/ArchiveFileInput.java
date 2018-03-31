/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.io;

import global.namespace.fun.io.api.Socket;

import java.io.Closeable;
import java.io.InputStream;
import java.util.Optional;
import java.util.zip.ZipEntry;

/**
 * An abstraction for reading an archive file.
 *
 * @see ArchiveFileOutput
 * @author Christian Schlichtherle
 */
public interface ArchiveFileInput extends Iterable<ZipEntry>, Closeable {

    /** Looks up the archive entry with the given name. */
    Optional<ZipEntry> entry(String name);

    /** Returns an input stream for reading the contents of the given archive entry. */
    Socket<InputStream> input(ZipEntry entry);
}
