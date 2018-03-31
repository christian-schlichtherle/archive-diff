/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.model;

import global.namespace.archive.diff.io.MessageDigests;
import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Source;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static global.namespace.fun.io.jaxb.JAXB.xmlCodec;
import static java.util.Collections.*;

/**
 * A Value Object which represents the meta data in a delta-archive file.
 * It encapsulates unmodifiable collections of changed, unchanged, added and
 * removed entry names and message digests in canonical string notation,
 * attributed with the message digest algorithm name and byte length.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "delta")
@XmlAccessorType(XmlAccessType.FIELD)
public final class DeltaModel implements Serializable {

    private static final long serialVersionUID = 0L;

    /**
     * The name of the entry which contains the marshalled delta model in a delta-archive file.
     * This should be the first entry in the delta-archive file.
     */
    public static final String ENTRY_NAME = "META-INF/delta.xml";

    @XmlAttribute(required = true)
    private final String algorithm;

    @XmlAttribute
    private final @CheckForNull Integer numBytes;

    @XmlJavaTypeAdapter(EntryNameAndTwoDigestsMapAdapter.class)
    private final Map<String, EntryNameAndTwoDigestValues> changed;

    @XmlJavaTypeAdapter(EntryNameAndDigestMapAdapter.class)
    private final Map<String, EntryNameAndDigestValue>
            unchanged, added, removed;

    /** Required for JAXB. */
    @SuppressWarnings("unused")
    private DeltaModel() {
        algorithm = "";
        numBytes = null;
        changed = emptyMap();
        unchanged = added = removed = emptyMap();
    }

    DeltaModel(final Builder b) {
        this.algorithm = b.messageDigest.getAlgorithm();
        this.numBytes = lengthBytes(b.messageDigest);
        this.changed = changedMap(b.changed);
        this.unchanged = unchangedMap(b.unchanged);
        this.added = unchangedMap(b.added);
        this.removed = unchangedMap(b.removed);
    }

    /** Returns a new builder for a delta model. */
    public static Builder builder() { return new Builder(); }

    private static @Nullable Integer lengthBytes(final MessageDigest digest) {
        try {
            final MessageDigest
                    clone = MessageDigests.create(digest.getAlgorithm());
            if (clone.getDigestLength() == digest.getDigestLength())
                return null;
        } catch (IllegalArgumentException ignored) {
        }
        return digest.getDigestLength();
    }

    static Map<String, EntryNameAndTwoDigestValues> changedMap(
            final Collection<EntryNameAndTwoDigestValues> entries) {
        final Map<String, EntryNameAndTwoDigestValues> map =
                new LinkedHashMap<>(initialCapacity(entries));
        for (EntryNameAndTwoDigestValues entryNameAndTwoDigestValues : entries)
            map.put(entryNameAndTwoDigestValues.name(), entryNameAndTwoDigestValues);
        return unmodifiableMap(map);
    }

    static Map<String, EntryNameAndDigestValue> unchangedMap(
            final Collection<EntryNameAndDigestValue> entries) {
        final Map<String, EntryNameAndDigestValue> map =
                new LinkedHashMap<>(initialCapacity(entries));
        for (EntryNameAndDigestValue entryNameAndDigestValue : entries)
            map.put(entryNameAndDigestValue.entryName(), entryNameAndDigestValue);
        return unmodifiableMap(map);
    }

    private static int initialCapacity(Collection<?> c) {
        return HashMaps.initialCapacity(c.size());
    }

    /** Returns the message digest algorithm name. */
    public String digestAlgorithmName() { return algorithm; }

    /**
     * Returns the message digest byte length.
     * This is {@code null} if and only if the byte length of the message
     * digest used to build this delta model is the default value for the
     * algorithm.
     */
    public @Nullable Integer digestByteLength() { return numBytes; }

    /**
     * Returns a collection of the entry name and two message digests for the
     * <i>changed</i> entries.
     */
    public Collection<EntryNameAndTwoDigestValues> changedEntries() {
        return changed.values();
    }

    /** Looks up the given entry name in the <i>changed</i> entries. */
    public EntryNameAndTwoDigestValues changed(String name) {
        return changed.get(name);
    }

    /**
     * Returns a collection of the entry name and message digest for the
     * <i>unchanged</i> entries.
     */
    public Collection<EntryNameAndDigestValue> unchangedEntries() {
        return unchanged.values();
    }

    /** Looks up the given entry name in the <i>unchanged</i> entries. */
    @Deprecated
    public EntryNameAndDigestValue unchanged(String name) {
        return unchanged.get(name);
    }

    /**
     * Returns a collection of the entry name and message digest for the
     * <i>added</i> entries.
     */
    public Collection<EntryNameAndDigestValue> addedEntries() {
        return added.values();
    }

    /** Looks up the given entry name in the <i>added</i> entries. */
    public EntryNameAndDigestValue added(String name) {
        return added.get(name);
    }

    /**
     * Returns a collection of the entry name and message digest for the
     * <i>removed</i> entries.
     */
    public Collection<EntryNameAndDigestValue> removedEntries() {
        return removed.values();
    }

    /** Looks up the given entry name in the <i>removed</i> entries. */
    @Deprecated
    public EntryNameAndDigestValue removed(String name) {
        return removed.get(name);
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DeltaModel)) {
            return false;
        }
        final DeltaModel that = (DeltaModel) obj;
        return  this.algorithm.equals(that.algorithm) &&
                Objects.equals(this.numBytes, that.numBytes) &&
                this.changed.equals(that.changed) &&
                this.unchanged.equals(that.unchanged) &&
                this.added.equals(that.added) &&
                this.removed.equals(that.removed);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + algorithm.hashCode();
        hash = 31 * hash + Objects.hashCode(numBytes);
        hash = 31 * hash + changed.hashCode();
        hash = 31 * hash + unchanged.hashCode();
        hash = 31 * hash + added.hashCode();
        hash = 31 * hash + removed.hashCode();
        return hash;
    }

    /**
     * Encodes this delta model to XML.
     *
     * @param sink the sink for writing the XML.
     * @throws Exception at the discretion of the JAXB codec, e.g. if the
     *         sink isn't writable.
     */
    public void encodeToXml(Sink sink) throws Exception { xmlCodec(jaxbContext()).encoder(sink).encode(this); }

    /**
     * Decodes a delta model from XML.
     *
     * @param source the source for reading the XML.
     * @return the decoded delta model.
     * @throws Exception at the discretion of the JAXB codec, e.g. if the
     *         source isn't readable.
     */
    public static DeltaModel decodeFromXml(Source source) throws Exception {
        return xmlCodec(jaxbContext()).decoder(source).decode(DeltaModel.class);
    }

    /** Returns a JAXB context which binds only this class. */
    public static JAXBContext jaxbContext() { return Lazy.JAXB_CONTEXT; }

    private static class Lazy {

        static final JAXBContext JAXB_CONTEXT;

        static {
            try { JAXB_CONTEXT = JAXBContext.newInstance(DeltaModel.class); }
            catch (JAXBException ex) { throw new AssertionError(ex); }
        }
    }

    /**
     * A builder for a delta model.
     * The default value for the collection of <i>unchanged</i>, <i>changed</i>,
     * <i>added</i> and <i>removed</i> entry names and message digests is an
     * empty collection.
     */
    @SuppressWarnings({
        "PackageVisibleField",
        "AssignmentToCollectionOrArrayFieldFromParameter"
    })
    public static final class Builder {

        @CheckForNull MessageDigest messageDigest;
        @CheckForNull Collection<EntryNameAndTwoDigestValues> changed = emptyList();
        @CheckForNull Collection<EntryNameAndDigestValue>
                unchanged = emptyList(),
                added = emptyList(),
                removed = emptyList();

        Builder() { }

        public Builder messageDigest(
                final @Nullable MessageDigest messageDigest) {
            this.messageDigest = messageDigest;
            return this;
        }

        public Builder changedEntries(
                final @Nullable Collection<EntryNameAndTwoDigestValues> changed) {
            this.changed = changed;
            return this;
        }

        public Builder unchangedEntries(
                final @Nullable Collection<EntryNameAndDigestValue> unchanged) {
            this.unchanged = unchanged;
            return this;
        }

        public Builder addedEntries(
                final @Nullable Collection<EntryNameAndDigestValue> added) {
            this.added = added;
            return this;
        }

        public Builder removedEntries(
                final @Nullable Collection<EntryNameAndDigestValue> removed) {
            this.removed = removed;
            return this;
        }

        public DeltaModel build() { return new DeltaModel(this); }
    }
}
