/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.io.dir;

import global.namespace.archive.io.api.ArchiveFileStore;

import java.io.File;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

/**
 * Provides access to directories as if they were archive files.
 * This is handy for testing purposes or if you want to diff/patch two directories.
 *
 * @author Christian Schlichtherle
 */
public class Dir {

    private Dir() { }

    /** Returns an archive file store for the given directory. */
    public static ArchiveFileStore<Path> directory(File directory) { return directory(directory.toPath()); }

    /** Returns an archive file store for the given directory. */
    public static ArchiveFileStore<Path> directory(Path directory) { return new DirectoryStore(requireNonNull(directory)); }
}
