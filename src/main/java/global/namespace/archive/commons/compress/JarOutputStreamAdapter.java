/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.commons.compress;

import global.namespace.archive.api.ArchiveFileEntry;
import global.namespace.archive.api.ArchiveFileOutput;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import static global.namespace.archive.commons.compress.CommonsCompress.archiveFileEntry;

/**
 * Adapts a {@link JarArchiveOutputStream} to an {@link ArchiveFileOutput}.
 *
 * @author Christian Schlichtherle
 */
final class JarOutputStreamAdapter extends ZipOutputStreamAdapter {

    JarOutputStreamAdapter(JarArchiveOutputStream jar) { super(jar); }

    @Override
    public ArchiveFileEntry<ZipArchiveEntry> entry(String name) { return archiveFileEntry(new JarArchiveEntry(name)); }
}
