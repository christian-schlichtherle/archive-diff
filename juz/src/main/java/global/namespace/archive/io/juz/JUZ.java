/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.io.juz;

import global.namespace.archive.io.api.ArchiveFileInput;
import global.namespace.archive.io.api.ArchiveFileOutput;
import global.namespace.archive.io.api.ArchiveFileStore;
import global.namespace.fun.io.api.Socket;

import java.io.File;
import java.io.FileOutputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static java.util.Objects.requireNonNull;

/**
 * Provides access to JAR and ZIP files using the package {@code java.util.zip} (JUZ).
 *
 * @author Christian Schlichtherle
 */
public class JUZ {

    private JUZ() { }

    /** Returns an archive file store for the given JAR file. */
    public static ArchiveFileStore<ZipEntry> jar(final File file) {
        requireNonNull(file);
        return new ArchiveFileStore<ZipEntry>() {

            @Override
            public Socket<ArchiveFileInput<ZipEntry>> input() { return () -> new ZipFileAdapter(new ZipFile(file)); }

            @Override
            public Socket<ArchiveFileOutput<ZipEntry>> output() {
                return () -> new JarOutputStreamAdapter(new JarOutputStream(new FileOutputStream(file)));
            }
        };
    }

    /** Returns an archive file store for the given ZIP file. */
    public static ArchiveFileStore<ZipEntry> zip(final File file) {
        requireNonNull(file);
        return new ArchiveFileStore<ZipEntry>() {

            @Override
            public Socket<ArchiveFileInput<ZipEntry>> input() { return () -> new ZipFileAdapter(new ZipFile(file)); }

            @Override
            public Socket<ArchiveFileOutput<ZipEntry>> output() {
                return () -> new ZipOutputStreamAdapter(new ZipOutputStream(new FileOutputStream(file)));
            }
        };
    }
}
