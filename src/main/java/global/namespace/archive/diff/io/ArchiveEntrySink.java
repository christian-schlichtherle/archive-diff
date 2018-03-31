/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.io;

import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Socket;

import java.io.OutputStream;
import java.util.zip.ZipEntry;

import static java.util.Objects.requireNonNull;

/**
 * Writes an archive entry to an archive file.
 *
 * @see ArchiveEntrySource
 * @author Christian Schlichtherle
 */
public final class ArchiveEntrySink implements Sink {

    private final ZipEntry entry;
    private final ArchiveFileOutput output;

    public ArchiveEntrySink(final ZipEntry entry, final ArchiveFileOutput output) {
        this.entry = requireNonNull(entry);
        this.output = requireNonNull(output);
    }

    /** Returns the entry name. */
    public String name() { return entry.getName(); }

    @Override
    public Socket<OutputStream> output() { return output.output(entry); }
}
