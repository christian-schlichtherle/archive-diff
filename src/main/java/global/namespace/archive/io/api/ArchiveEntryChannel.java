/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.io.api;

import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Source;

/**
 * An abstraction for copying entries from an {@link ArchiveEntrySource} to an {@link ArchiveEntrySink}.
 *
 * @see ArchiveEntrySource#connect(ArchiveEntrySink)
 * @see ArchiveEntrySink#connect(ArchiveEntrySource)
 * @author Christian Schlichtherle
 */
public interface ArchiveEntryChannel {

    /** Returns the source. */
    Source source();

    /** Returns the sink. */
    Sink sink();
}
