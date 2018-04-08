/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.juz;

import global.namespace.archive.api.ArchiveFileEntry;
import global.namespace.archive.api.ArchiveFileInput;
import global.namespace.fun.io.api.Socket;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static global.namespace.archive.juz.JUZ.archiveFileEntry;
import static java.util.Objects.requireNonNull;

/**
 * Adapts a {@link ZipFile} to an {@link ArchiveFileInput}.
 *
 * @author Christian Schlichtherle
 */
final class ZipFileAdapter implements ArchiveFileInput<ZipEntry> {

    private final ZipFile zip;

    ZipFileAdapter(final ZipFile input) { this.zip = requireNonNull(input); }

    @Override
    public Iterator<ArchiveFileEntry<ZipEntry>> iterator() {
        return new Iterator<ArchiveFileEntry<ZipEntry>>() {

            final Enumeration<? extends ZipEntry> en = zip.entries();

            @Override
            public boolean hasNext() { return en.hasMoreElements(); }

            @Override
            public ArchiveFileEntry<ZipEntry> next() { return archiveFileEntry(en.nextElement()); }

            @Override
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }

    @Override
    public Optional<ArchiveFileEntry<ZipEntry>> entry(String name) {
        return Optional.ofNullable(zip.getEntry(name)).map(JUZ::archiveFileEntry);
    }

    @Override
    public Socket<InputStream> input(ArchiveFileEntry<ZipEntry> entry) {
        return () -> zip.getInputStream(entry.entry());
    }

    @Override
    public void close() throws IOException { zip.close(); }
}
