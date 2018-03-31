/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import global.namespace.archive.diff.spi.ArchiveFileInput;
import global.namespace.fun.io.api.Socket;
import global.namespace.fun.io.api.Source;
import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.InputStream;

interface ArchiveEntrySource extends Source {

    static ArchiveEntrySource create(ArchiveEntry entry, ArchiveFileInput input) {
        return new ArchiveEntrySource() {

            public String name() { return entry.getName(); }

            public Socket<InputStream> input() { return input.input(entry); }
        };
    }

    String name();
}
