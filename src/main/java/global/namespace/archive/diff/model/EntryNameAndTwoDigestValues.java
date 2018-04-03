/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.model;

import static java.util.Objects.requireNonNull;

/**
 * A Value Object which represents a archive entry name and two message digests in canonical string notation.
 *
 * @author Christian Schlichtherle
 */
public final class EntryNameAndTwoDigestValues {

    private final String entryName, firstDigestValue, secondDigestValue;

    /**
     * Default constructor.
     * The first and second message digest should not be equal.
     */
    public EntryNameAndTwoDigestValues(
            final String entryName,
            final String firstDigestValue,
            final String secondDigestValue) {
        this.entryName = requireNonNull(entryName);
        this.firstDigestValue = requireNonNull(firstDigestValue);
        this.secondDigestValue = requireNonNull(secondDigestValue);
        assert !firstDigestValue.equals(secondDigestValue);
    }

    /** Returns the entry name. */
    public String entryName() { return entryName; }

    /** Returns the first message digest value. */
    public String firstDigestValue() { return firstDigestValue; }

    /** Returns the second message digest value. */
    public String secondDigestValue() { return secondDigestValue; }

    /** Returns the first archive entry name and digest value. */
    @Deprecated
    public EntryNameAndDigestValue firstEntryNameAndDigestValue() {
        return new EntryNameAndDigestValue(entryName(), firstDigestValue());
    }

    /** Returns the second archive entry name and digest value. */
    public EntryNameAndDigestValue secondEntryNameAndDigestValue() {
        return new EntryNameAndDigestValue(entryName(), secondDigestValue());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntryNameAndTwoDigestValues)) {
            return false;
        }
        final EntryNameAndTwoDigestValues that = (EntryNameAndTwoDigestValues) obj;
        return  this.entryName().equals(that.entryName()) &&
                this.firstDigestValue().equals(that.firstDigestValue()) &&
                this.secondDigestValue().equals(that.secondDigestValue());
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + entryName().hashCode();
        hash = 31 * hash + firstDigestValue().hashCode();
        hash = 31 * hash + secondDigestValue().hashCode();
        return hash;
    }
}
