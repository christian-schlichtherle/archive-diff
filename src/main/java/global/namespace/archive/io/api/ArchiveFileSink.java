/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.io.api;

import global.namespace.fun.io.api.Socket;
import global.namespace.fun.io.api.function.XConsumer;
import global.namespace.fun.io.api.function.XFunction;

/**
 * An abstraction for safe writing of archive entries to an archive file without leaking resources.
 * An archive file sink provides a {@linkplain #output() socket} for safe access to an {@link ArchiveFileOutput}.
 *
 * @author Christian Schlichtherle
 */
public interface ArchiveFileSink<E> {

    /** Returns the underlying archive file output socket for writing the archive entries. */
    Socket<ArchiveFileOutput<E>> output();

    /**
     * Loans an archive file output from the underlying {@linkplain #output() socket} to the given consumer.
     * The archive file output will be closed upon return from this method.
     */
    default void acceptWriter(XConsumer<? super ArchiveFileOutput<E>> writer) throws Exception { output().accept(writer); }

    /**
     * Loans an archive file output from the underlying {@linkplain #output() socket} to the given function
     * and returns its value.
     * The archive file output will be closed upon return from this method.
     * <p>
     * It is an error to return the loaned archive file output from the given function or any other object which holds
     * on to it.
     */
    default <U> U applyWriter(XFunction<? super ArchiveFileOutput<E>, ? extends U> writer) throws Exception {
        return output().apply(writer);
    }
}
