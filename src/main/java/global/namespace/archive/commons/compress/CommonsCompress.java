/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.commons.compress;

import global.namespace.archive.api.ArchiveFileEntry;
import global.namespace.archive.api.ArchiveFileInput;
import global.namespace.archive.api.ArchiveFileOutput;
import global.namespace.archive.api.ArchiveFileStore;
import global.namespace.fun.io.api.Socket;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.FileOutputStream;

import static java.util.Objects.requireNonNull;

/**
 * Provides access to JAR and ZIP files using Apache Commons Compress.
 *
 * @author Christian Schlichtherle
 */
public class CommonsCompress {

    private CommonsCompress() { }

    /** Returns an archive file store for the given JAR file. */
    public static ArchiveFileStore<ZipArchiveEntry> jar(final File file) {
        requireNonNull(file);
        return new ArchiveFileStore<ZipArchiveEntry>() {

            @Override
            public Socket<ArchiveFileInput<ZipArchiveEntry>> input() {
                return () -> new ZipFileAdapter(new ZipFile(file));
            }

            @Override
            public Socket<ArchiveFileOutput<ZipArchiveEntry>> output() {
                return () -> new JarOutputStreamAdapter(new JarArchiveOutputStream(new FileOutputStream(file)));
            }
        };
    }

    /** Returns an archive file store for the given ZIP file. */
    public static ArchiveFileStore<ZipArchiveEntry> zip(final File file) {
        requireNonNull(file);
        return new ArchiveFileStore<ZipArchiveEntry>() {

            @Override
            public Socket<ArchiveFileInput<ZipArchiveEntry>> input() {
                return () -> new ZipFileAdapter(new ZipFile(file));
            }

            @Override
            public Socket<ArchiveFileOutput<ZipArchiveEntry>> output() {
                return () -> new ZipOutputStreamAdapter(new ZipArchiveOutputStream(file));
            }
        };
    }

    static ArchiveFileEntry<ZipArchiveEntry> archiveFileEntry(ZipArchiveEntry entry) {
        return new ArchiveFileEntry<ZipArchiveEntry>() {

            public String name() { return entry.getName(); }

            @Override
            public boolean isDirectory() { return entry.isDirectory(); }

            public ZipArchiveEntry entry() { return entry; }
        };
    }
}
