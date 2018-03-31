/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.io;

import javax.annotation.WillCloseWhenClosed;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Adapts a {@link JarOutputStream} to an {@link ArchiveFileOutput}.
 *
 * @author Christian Schlichtherle
 */
class JarOutputStreamAdapter extends ZipOutputStreamAdapter {

    JarOutputStreamAdapter(@WillCloseWhenClosed JarOutputStream jar) { super(jar); }

    @Override
    public ZipEntry entry(String name) { return new JarEntry(name); }
}
