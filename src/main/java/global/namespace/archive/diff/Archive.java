/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import global.namespace.archive.diff.spi.ArchiveFileInput;
import global.namespace.archive.diff.spi.ArchiveFileOutput;
import global.namespace.archive.diff.spi.ArchiveFileStore;
import global.namespace.fun.io.api.Socket;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.FileOutputStream;

import static java.util.Objects.requireNonNull;

/**
 * A static factory for providing access to JAR and ZIP files.
 *
 * @author Christian Schlichtherle
 */
public class Archive {

    private Archive() { }

    /** Returns an archive file store for the given JAR file. */
    public static ArchiveFileStore jar(final File file) {
        requireNonNull(file);
        return new ArchiveFileStore() {

            @Override
            public Socket<ArchiveFileInput> input() { return () -> new ZipFileAdapter(new ZipFile(file)); }

            @Override
            public Socket<ArchiveFileOutput> output() {
                return () -> new JarOutputStreamAdapter(new JarArchiveOutputStream(new FileOutputStream(file)));
            }
        };
    }

    /** Returns an archive file store for the given ZIP file. */
    public static ArchiveFileStore zip(final File file) {
        requireNonNull(file);
        return new ArchiveFileStore() {

            @Override
            public Socket<ArchiveFileInput> input() { return () -> new ZipFileAdapter(new ZipFile(file)); }

            @Override
            public Socket<ArchiveFileOutput> output() {
                return () -> new ZipOutputStreamAdapter(new ZipArchiveOutputStream(file));
            }
        };
    }

    public static ArchiveFileDiff.Builder diff() { return ArchiveFileDiff.builder(); }

    public static ArchiveFilePatch.Builder patch() { return ArchiveFilePatch.builder(); }
}
