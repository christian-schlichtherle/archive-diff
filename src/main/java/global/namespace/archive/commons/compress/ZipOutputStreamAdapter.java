/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.commons.compress;

import global.namespace.archive.api.ArchiveFileEntry;
import global.namespace.archive.api.ArchiveFileOutput;
import global.namespace.fun.io.api.Socket;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static global.namespace.archive.commons.compress.CommonsCompress.archiveFileEntry;
import static java.util.Objects.requireNonNull;

/**
 * Adapts a {@link ZipArchiveOutputStream} to an {@link ArchiveFileOutput}.
 *
 * @author Christian Schlichtherle
 */
class ZipOutputStreamAdapter implements ArchiveFileOutput<ZipArchiveEntry> {

    private final ZipArchiveOutputStream zip;

    ZipOutputStreamAdapter(final ZipArchiveOutputStream zip) { this.zip = requireNonNull(zip); }

    @Override
    public ArchiveFileEntry<ZipArchiveEntry> entry(String name) { return archiveFileEntry(new ZipArchiveEntry(name)); }

    @Override
    public Socket<OutputStream> output(final ArchiveFileEntry<ZipArchiveEntry> entry) {
        return () -> {
            final ZipArchiveEntry zipEntry = entry.entry();
            if (zipEntry.isDirectory()) {
                zipEntry.setMethod(ZipArchiveOutputStream.STORED);
                zipEntry.setSize(0);
                zipEntry.setCompressedSize(0);
                zipEntry.setCrc(0);
            }
            zip.putArchiveEntry(zipEntry);
            return new FilterOutputStream(zip) {

                boolean closed;

                @Override public void close() throws IOException {
                    if (!closed) {
                        closed = true;
                        ((ZipArchiveOutputStream) out).closeArchiveEntry(); // not idempotent!
                    }
                }
            };
        };
    }

    @Override
    public void close() throws IOException { zip.close(); }
}
