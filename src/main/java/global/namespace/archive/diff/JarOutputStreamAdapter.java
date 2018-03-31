/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import global.namespace.archive.diff.spi.ArchiveFileOutput;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;

import javax.annotation.WillCloseWhenClosed;

/**
 * Adapts a {@link JarArchiveOutputStream} to an {@link ArchiveFileOutput}.
 *
 * @author Christian Schlichtherle
 */
final class JarOutputStreamAdapter extends ZipOutputStreamAdapter {

    JarOutputStreamAdapter(@WillCloseWhenClosed JarArchiveOutputStream jar) { super(jar); }

    @Override
    public ArchiveEntry entry(String name) { return new JarArchiveEntry(name); }
}
