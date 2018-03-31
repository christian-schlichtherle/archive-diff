/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.fun.io.zip.io;

import global.namespace.fun.io.api.Socket;
import global.namespace.fun.io.api.Source;

import java.io.InputStream;
import java.util.zip.ZipEntry;

import static java.util.Objects.requireNonNull;

/**
 * Reads a ZIP entry from a ZIP input.
 *
 * @see ZipEntrySink
 * @author Christian Schlichtherle
 */
public final class ZipEntrySource implements Source {

    private final ZipEntry entry;
    private final ZipInput input;

    public ZipEntrySource(final ZipEntry entry, final ZipInput input) {
        this.entry = requireNonNull(entry);
        this.input = requireNonNull(input);
    }

    /** Returns the entry name. */
    public String name() { return entry.getName(); }

    @Override
    public Socket<InputStream> input() { return input.input(entry); }
}
