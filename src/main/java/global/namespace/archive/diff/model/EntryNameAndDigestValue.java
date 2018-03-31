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
 * A Value Object which represents an archive entry name and message digest in canonical string notation.
 *
 * @author Christian Schlichtherle
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
public final class EntryNameAndDigestValue implements Serializable {

    private static final long serialVersionUID = 0L;

    @XmlAttribute(required = true)
    private final String name, digest;

    /** Required for JAXB. */
    private EntryNameAndDigestValue() { name = digest = ""; }

    public EntryNameAndDigestValue(final String entryName, final String digestValue) {
        this.name = requireNonNull(entryName);
        this.digest = requireNonNull(digestValue);
    }

    /** Returns the entry name. */
    public String entryName() { return name; }

    /** Returns the value of the message digest. */
    public String digestValue() { return digest; }

    @Override public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntryNameAndDigestValue)) {
            return false;
        }
        final EntryNameAndDigestValue that = (EntryNameAndDigestValue) obj;
        return this.entryName().equals(that.entryName()) && this.digestValue().equals(that.digestValue());
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + entryName().hashCode();
        hash = 31 * hash + digestValue().hashCode();
        return hash;
    }
}
