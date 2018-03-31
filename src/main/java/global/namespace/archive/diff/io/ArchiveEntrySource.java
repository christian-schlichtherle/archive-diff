/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.io;

import global.namespace.fun.io.api.Socket;
import global.namespace.fun.io.api.Source;

import java.io.InputStream;
import java.util.zip.ZipEntry;

import static java.util.Objects.requireNonNull;

/**
 * Reads an archive entry from an archive file.
 *
 * @see ArchiveEntrySink
 * @author Christian Schlichtherle
 */
public final class ArchiveEntrySource implements Source {

    private final ZipEntry entry;
    private final ArchiveFileInput input;

    public ArchiveEntrySource(final ZipEntry entry, final ArchiveFileInput input) {
        this.entry = requireNonNull(entry);
        this.input = requireNonNull(input);
    }

    /** Returns the entry name. */
    public String name() { return entry.getName(); }

    @Override
    public Socket<InputStream> input() { return input.input(entry); }
}
