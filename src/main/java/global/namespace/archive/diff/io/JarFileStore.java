/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.io;

import global.namespace.fun.io.api.Socket;

import java.io.File;
import java.io.FileOutputStream;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import static java.util.Objects.requireNonNull;

/**
 * A file based source and sink for JAR files.
 *
 * @see ZipFileAdapter
 * @see JarOutputStreamAdapter
 * @author Christian Schlichtherle
 */
public final class JarFileStore implements ArchiveFileSource, ArchiveFileSink {

    private final File file;

    public JarFileStore(final File file) { this.file = requireNonNull(file); }

    @Override
    public Socket<ArchiveFileInput> input() { return () -> new ZipFileAdapter(new JarFile(file)); }

    @Override
    public Socket<ArchiveFileOutput> output() {
        return ((Socket<FileOutputStream>) (() -> new FileOutputStream(file)))
                .map(out -> new JarOutputStreamAdapter(new JarOutputStream(out)));
    }
}
