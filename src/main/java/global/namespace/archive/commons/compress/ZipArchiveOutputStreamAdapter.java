/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.commons.compress;

import global.namespace.archive.api.ArchiveEntrySink;
import global.namespace.archive.api.ArchiveEntrySource;
import global.namespace.archive.api.ArchiveFileOutput;
import global.namespace.fun.io.api.Socket;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Adapts a {@link ZipArchiveOutputStream} to an {@link ArchiveFileOutput}.
 *
 * @author Christian Schlichtherle
 */
class ZipArchiveOutputStreamAdapter implements ArchiveFileOutput<ZipArchiveEntry> {

    private static final Pattern COMPRESSED_FILE_EXTENSIONS =
            Pattern.compile(".*\\.(ear|jar|war|zip|gz|xz)", Pattern.CASE_INSENSITIVE);

    private final ZipArchiveOutputStream zip;

    ZipArchiveOutputStreamAdapter(final ZipArchiveOutputStream zip) { this.zip = requireNonNull(zip); }

    /** Returns {@code false}. */
    public boolean isJar() { return false; }

    public ArchiveEntrySink<ZipArchiveEntry> sink(String name) { return sink(new ZipArchiveEntry(name)); }

    ArchiveEntrySink<ZipArchiveEntry> sink(ZipArchiveEntry entry) {
        return new ArchiveEntrySink<ZipArchiveEntry>() {

            public String name() { return entry.getName(); }

            public boolean isDirectory() { return entry.isDirectory(); }

            public ZipArchiveEntry entry() { return entry; }

            public Socket<OutputStream> output(final ArchiveEntrySource<?> source) {
                // TODO: This should be deferred until the underlying socket is used to create an output stream.
                if (source.entry() instanceof ZipArchiveEntry && COMPRESSED_FILE_EXTENSIONS.matcher(source.name()).matches()) {
                    final ZipArchiveEntry origin = (ZipArchiveEntry) source.entry();
                    final long size = origin.getSize();

                    entry.setMethod(ZipArchiveOutputStream.STORED);
                    entry.setSize(size);
                    entry.setCompressedSize(size);
                    entry.setCrc(origin.getCrc());
                }
                return output();
            }

            public Socket<OutputStream> output() {
                return () -> {
                    if (entry.isDirectory()) {
                        entry.setMethod(ZipArchiveOutputStream.STORED);
                        entry.setSize(0);
                        entry.setCompressedSize(0);
                        entry.setCrc(0);
                    }
                    zip.putArchiveEntry(entry);
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
        };
    }

    @Override
    public void close() throws IOException { zip.close(); }
}
