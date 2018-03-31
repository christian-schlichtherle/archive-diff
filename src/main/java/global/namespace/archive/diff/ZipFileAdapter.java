/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import global.namespace.archive.diff.spi.ArchiveFileInput;
import global.namespace.fun.io.api.Socket;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Adapts a {@link ZipFile} to a {@link ArchiveFileInput}.
 *
 * @author Christian Schlichtherle
 */
final class ZipFileAdapter implements ArchiveFileInput {

    private final ZipFile zip;

    ZipFileAdapter(final ZipFile input) { this.zip = requireNonNull(input); }

    @Override
    public Iterator<ArchiveEntry> iterator() {
        return new Iterator<ArchiveEntry>() {

            final Enumeration<? extends ArchiveEntry> en = zip.getEntries();

            @Override
            public boolean hasNext() { return en.hasMoreElements(); }

            @Override
            public ArchiveEntry next() { return en.nextElement(); }

            @Override
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }

    @Override
    public Optional<ArchiveEntry> entry(String name) { return Optional.ofNullable(zip.getEntry(name)); }

    @Override
    public Socket<InputStream> input(ArchiveEntry entry) { return () -> zip.getInputStream((ZipArchiveEntry) entry); }

    @Override
    public void close() throws IOException { zip.close(); }
}
