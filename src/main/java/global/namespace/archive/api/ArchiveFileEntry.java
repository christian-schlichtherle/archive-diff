/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.api;

/**
 * Adapts an underlying archive entry.
 *
 * @author Christian Schlichtherle
 */
public abstract class ArchiveFileEntry<E> {

    /** Returns the name of the underlying archive entry. */
    public abstract String name();

    /** Returns {@code true} if and only if the underlying entry is a directory. */
    public abstract boolean isDirectory();

    /** Returns the underlying archive entry. */
    public abstract E entry();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ArchiveFileEntry)) {
            return false;
        }
        final ArchiveFileEntry that = (ArchiveFileEntry) obj;
        return this.entry().equals(that.entry());
    }

    @Override
    public int hashCode() { return entry().hashCode(); }

    @Override
    public String toString() { return entry().toString(); }
}
