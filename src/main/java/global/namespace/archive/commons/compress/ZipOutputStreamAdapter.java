/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.commons.compress;

import global.namespace.archive.api.ArchiveEntrySink;
import global.namespace.archive.api.ArchiveEntrySource;
import global.namespace.archive.api.ArchiveFileEntry;
import global.namespace.archive.api.ArchiveFileOutput;
import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Socket;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

import static global.namespace.archive.commons.compress.CommonsCompress.archiveFileEntry;
import static java.util.Objects.requireNonNull;

/**
 * Adapts a {@link ZipArchiveOutputStream} to an {@link ArchiveFileOutput}.
 *
 * @author Christian Schlichtherle
 */
class ZipOutputStreamAdapter implements ArchiveFileOutput<ZipArchiveEntry> {

    private static final Pattern COMPRESSED_FILE_EXTENSIONS =
            Pattern.compile(".*\\.(ear|jar|war|zip|gz|xz)", Pattern.CASE_INSENSITIVE);

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

                @Override
                public void close() throws IOException {
                    if (!closed) {
                        closed = true;
                        ((ZipArchiveOutputStream) out).closeArchiveEntry(); // not idempotent!
                    }
                }
            };
        };
    }

    @Override
    public ArchiveEntrySink<ZipArchiveEntry> sink(ArchiveFileEntry<ZipArchiveEntry> entry) {
        return new ArchiveEntrySink<ZipArchiveEntry>() {

            final ZipArchiveEntry zipEntry = entry.entry();

            public String name() { return entry.name(); }

            public ZipArchiveEntry entry() { return zipEntry; }

            public Socket<OutputStream> output() { return ZipOutputStreamAdapter.this.output(entry); }

            @Override
            public Sink prepareForCopyFrom(final ArchiveEntrySource<?> source) {
                // TODO: This should be deferred until the underlying socket is used to vend an output stream.
                if (source.entry() instanceof ZipArchiveEntry && COMPRESSED_FILE_EXTENSIONS.matcher(source.name()).matches()) {
                    final ZipArchiveEntry sourceEntry = (ZipArchiveEntry) source.entry();
                    final long size = sourceEntry.getSize();

                    zipEntry.setMethod(ZipArchiveOutputStream.STORED);
                    zipEntry.setSize(size);
                    zipEntry.setCompressedSize(size);
                    zipEntry.setCrc(sourceEntry.getCrc());
                }
                return this;
            }
        };
    }

    @Override
    public void close() throws IOException { zip.close(); }
}
