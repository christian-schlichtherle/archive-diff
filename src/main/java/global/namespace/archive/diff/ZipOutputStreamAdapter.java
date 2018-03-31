/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import global.namespace.archive.diff.spi.ArchiveFileOutput;
import global.namespace.fun.io.api.Socket;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import javax.annotation.WillCloseWhenClosed;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

/**
 * Adapts a {@link ZipArchiveOutputStream} to an {@link ArchiveFileOutput}.
 *
 * @author Christian Schlichtherle
 */
class ZipOutputStreamAdapter implements ArchiveFileOutput {

    private final ZipArchiveOutputStream zip;

    ZipOutputStreamAdapter(final @WillCloseWhenClosed ZipArchiveOutputStream zip) { this.zip = requireNonNull(zip); }

    @Override
    public ArchiveEntry entry(String name) { return new ZipArchiveEntry(name); }

    @Override
    public Socket<OutputStream> output(final ArchiveEntry entry) {
        return () -> {
            final ZipArchiveEntry zipEntry = (ZipArchiveEntry) entry;
            if (zipEntry.isDirectory()) {
                zipEntry.setMethod(ZipArchiveOutputStream.STORED);
                zipEntry.setSize(0);
                zipEntry.setCompressedSize(0);
                zipEntry.setCrc(0);
            }
            zip.putArchiveEntry(zipEntry);
            return new FilterOutputStream(zip) {
                @Override public void close() throws IOException {
                    ((ZipArchiveOutputStream) out).closeArchiveEntry();
                }
            };
        };
    }

    @Override
    public void close() throws IOException { zip.close(); }
}
