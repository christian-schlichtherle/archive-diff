/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import global.namespace.archive.diff.spi.ArchiveFileOutput;
import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Socket;
import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.OutputStream;

interface ArchiveEntrySink extends Sink {

    static ArchiveEntrySink create(ArchiveEntry entry, ArchiveFileOutput output) {
        return new ArchiveEntrySink() {

            public String name() { return entry.getName(); }

            public Socket<OutputStream> output() { return output.output(entry); }
        };
    }

    String name();
}
