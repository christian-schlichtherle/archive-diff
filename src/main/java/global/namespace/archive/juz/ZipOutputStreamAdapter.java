/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.juz;

import global.namespace.archive.api.ArchiveFileEntry;
import global.namespace.archive.api.ArchiveFileOutput;
import global.namespace.fun.io.api.Socket;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static global.namespace.archive.juz.JUZ.archiveFileEntry;
import static java.util.Objects.requireNonNull;

/**
 * Adapts a {@link ZipOutputStream} to an {@link ArchiveFileOutput}.
 *
 * @author Christian Schlichtherle
 */
class ZipOutputStreamAdapter implements ArchiveFileOutput<ZipEntry> {

    private final ZipOutputStream zip;

    ZipOutputStreamAdapter(final ZipOutputStream zip) { this.zip = requireNonNull(zip); }

    @Override
    public ArchiveFileEntry<ZipEntry> entry(String name) { return archiveFileEntry(new ZipEntry(name)); }

    @Override
    public Socket<OutputStream> output(final ArchiveFileEntry<ZipEntry> entry) {
        return () -> {
            final ZipEntry zipEntry = entry.entry();
            if (zipEntry.isDirectory()) {
                zipEntry.setMethod(ZipOutputStream.STORED);
                zipEntry.setSize(0);
                zipEntry.setCompressedSize(0);
                zipEntry.setCrc(0);
            }
            zip.putNextEntry(zipEntry);
            return new FilterOutputStream(zip) {

                @Override
                public void close() throws IOException {
                    ((ZipOutputStream) out).closeEntry(); // not idempotent!
                }
            };
        };
    }

    @Override
    public void close() throws IOException { zip.close(); }
}
