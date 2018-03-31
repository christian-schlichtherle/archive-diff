/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.model;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

import static java.util.Objects.requireNonNull;

/**
 * A Value Object which represents a archive entry name and two message digests in canonical string notation.
 *
 * @author Christian Schlichtherle
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
public final class EntryNameAndTwoDigestValues implements Serializable {

    private static final long serialVersionUID = 0L;

    @XmlAttribute(required = true)
    private final String name, first, second;

    /** Required for JAXB. */
    private EntryNameAndTwoDigestValues() { name = first = second = ""; }

    /**
     * Default constructor.
     * The first and second message digest should not be equal.
     */
    public EntryNameAndTwoDigestValues(
            final String entryName,
            final String firstDigestValue,
            final String secondDigestValue) {
        this.name = requireNonNull(entryName);
        this.first = requireNonNull(firstDigestValue);
        this.second = requireNonNull(secondDigestValue);
        assert !firstDigestValue.equals(secondDigestValue);
    }

    /** Returns the entry name. */
    public String name() { return name; }

    /** Returns the first message digest value. */
    public String first() { return first; }

    /** Returns the second message digest value. */
    public String second() { return second; }

    /** Returns the first archive entry name and digest value. */
    @Deprecated
    public EntryNameAndDigestValue firstEntryNameAndDigestValue() {
        return new EntryNameAndDigestValue(name(), first());
    }

    /** Returns the second archive entry name and digest value. */
    public EntryNameAndDigestValue secondEntryNameAndDigestValue() {
        return new EntryNameAndDigestValue(name(), second());
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntryNameAndTwoDigestValues)) {
            return false;
        }
        final EntryNameAndTwoDigestValues that = (EntryNameAndTwoDigestValues) obj;
        return  this.name().equals(that.name()) &&
                this.first().equals(that.first()) &&
                this.second().equals(that.second());
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + name().hashCode();
        hash = 31 * hash + first().hashCode();
        hash = 31 * hash + second().hashCode();
        return hash;
    }
}
