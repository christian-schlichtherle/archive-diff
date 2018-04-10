/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.io.delta.model;

import static java.util.Objects.requireNonNull;

/**
 * A Value Object which represents an archive entry name and message digest in canonical string notation.
 *
 * @author Christian Schlichtherle
 */
public final class EntryNameAndDigestValue {

    private final String entryName, digestValue;

    public EntryNameAndDigestValue(final String entryName, final String digestValue) {
        this.entryName = requireNonNull(entryName);
        this.digestValue = requireNonNull(digestValue);
    }

    /** Returns the entry name. */
    public String entryName() { return entryName; }

    /** Returns the value of the message digest. */
    public String digestValue() { return digestValue; }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntryNameAndDigestValue)) {
            return false;
        }
        final EntryNameAndDigestValue that = (EntryNameAndDigestValue) obj;
        return this.entryName().equals(that.entryName()) && this.digestValue().equals(that.digestValue());
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + entryName().hashCode();
        hash = 31 * hash + digestValue().hashCode();
        return hash;
    }
}
