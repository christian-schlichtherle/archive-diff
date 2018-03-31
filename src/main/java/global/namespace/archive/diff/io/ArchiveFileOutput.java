/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.io;

import global.namespace.fun.io.api.Socket;
import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.Closeable;
import java.io.OutputStream;

/**
 * An abstraction for writing an archive.
 *
 * @see ArchiveFileInput
 * @author Christian Schlichtherle
 */
public interface ArchiveFileOutput extends Closeable {

    /** Returns a <em>new</em> archive entry. */
    ArchiveEntry entry(String name);

    /** Returns an output stream socket for writing the contents of the given archive entry. */
    Socket<OutputStream> output(ArchiveEntry entry);
}
