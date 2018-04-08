/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.juz;

import global.namespace.archive.api.ArchiveFileEntry;
import global.namespace.archive.api.ArchiveFileOutput;

import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static global.namespace.archive.juz.JUZ.archiveFileEntry;

/**
 * Adapts a {@link JarOutputStream} to an {@link ArchiveFileOutput}.
 *
 * @author Christian Schlichtherle
 */
final class JarOutputStreamAdapter extends ZipOutputStreamAdapter {

    JarOutputStreamAdapter(JarOutputStream jar) { super(jar); }

    @Override
    public ArchiveFileEntry<ZipEntry> entry(String name) { return archiveFileEntry(new JarEntry(name)); }
}
