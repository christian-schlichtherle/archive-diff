/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.diff;

import global.namespace.archive.diff.io.*;
import global.namespace.archive.diff.model.DeltaModel;
import global.namespace.archive.diff.model.EntryNameAndDigest;
import global.namespace.archive.diff.model.EntryNameAndTwoDigests;
import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Source;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import javax.annotation.WillNotClose;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static java.util.Optional.empty;

/**
 * Compares a <it>from-archive file</it> to a <it>to-archive file</it> and generates a <it>delta-archive file</it>.
 *
 * @author Christian Schlichtherle
 */
public abstract class ArchiveFileDiff {

    /** Returns a new builder for an archive file diff. */
    public static Builder builder() { return new Builder(); }

    /** Writes the output to the given delta-archive file. */
    public abstract void outputTo(ArchiveFileSink delta) throws Exception;

    /**
     * A builder for an archive file diff.
     * The default message digest is SHA-1.
     */
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "ConstantConditions"})
    public static class Builder {

        private Optional<String> digest = empty();

        private Optional<ArchiveFileSource> from = empty(), to = empty();

        private Builder() { }

        public Builder digest(final String digest) {
            this.digest = Optional.of(digest);
            return this;
        }

        public Builder from(final ArchiveFileSource from) {
            this.from = Optional.of(from);
            return this;
        }

        public Builder to(final ArchiveFileSource to) {
            this.to = Optional.of(to);
            return this;
        }

        public ArchiveFileDiff build() { return create(from.get(), to.get(), digest); }

        private static ArchiveFileDiff create(final ArchiveFileSource fromSource, final ArchiveFileSource toSource, final Optional<String> digestName) {
            return new ArchiveFileDiff() {

                @Override
                public void outputTo(final ArchiveFileSink deltaSink) throws Exception {
                    fromSource.acceptReader(from ->
                            toSource.acceptReader(to ->
                                    deltaSink.acceptWriter(delta ->
                                            new Engine() {

                                                final MessageDigest digest =
                                                        MessageDigests.create(digestName.orElse("SHA-1"));

                                                public MessageDigest digest() { return digest; }

                                                public ArchiveFileInput from() { return from; }

                                                public ArchiveFileInput to() { return to; }
                                            }.outputTo(delta)
                                    )
                            )
                    );
                }
            };
        }
    }

    public abstract static class Engine {

        private static final Pattern COMPRESSED_FILE_EXTENSIONS =
                Pattern.compile(".*\\.(ear|jar|war|zip|gz|xz)", Pattern.CASE_INSENSITIVE);

        /** Returns the message digest. */
        protected abstract MessageDigest digest();

        /** Returns the from-archive file. */
        protected abstract @WillNotClose ArchiveFileInput from();

        /** Returns the to-archive file. */
        protected abstract @WillNotClose ArchiveFileInput to();

        /** Writes the output to the given delta-archive file. */
        public void outputTo(final @WillNotClose ArchiveFileOutput delta) throws Exception {

            final class Streamer {

                private final DeltaModel model = model();

                private Streamer() throws Exception { model.encodeToXml(deltaEntrySink(deltaEntry(DeltaModel.ENTRY_NAME))); }

                private void stream() throws Exception {
                    for (final ArchiveEntry toEntry : to()) {
                        final String name = toEntry.getName();
                        if (changedOrAdded(name)) {
                            final ArchiveEntry deltaEntry = deltaEntry(name);
                            if (toEntry instanceof ZipArchiveEntry && deltaEntry instanceof ZipArchiveEntry &&
                                    COMPRESSED_FILE_EXTENSIONS.matcher(name).matches()) {
                                final ZipArchiveEntry zipToEntry = (ZipArchiveEntry) toEntry;
                                final ZipArchiveEntry zipDeltaEntry = (ZipArchiveEntry) deltaEntry;
                                final long size = zipToEntry.getSize();

                                zipDeltaEntry.setMethod(ZipArchiveOutputStream.STORED);
                                zipDeltaEntry.setSize(size);
                                zipDeltaEntry.setCompressedSize(size);
                                zipDeltaEntry.setCrc(zipToEntry.getCrc());
                            }
                            Copy.copy(toEntrySource(toEntry), deltaEntrySink(deltaEntry));
                        }
                    }
                }

                private Source toEntrySource(ArchiveEntry toEntry) { return new ArchiveEntrySource(toEntry, to()); }

                private Sink deltaEntrySink(ArchiveEntry deltaEntry) { return new ArchiveEntrySink(deltaEntry, delta); }

                private ArchiveEntry deltaEntry(String name) { return delta.entry(name); }

                private boolean changedOrAdded(String name) {
                    return null != model.changed(name) || null != model.added(name);
                }
            }

            new Streamer().stream();
        }

        /** Computes a delta model from the two input archive files. */
        public DeltaModel model() throws Exception { return new Assembler().walkAndReturn(new Assembly()).deltaModel(); }

        private class Assembler {

            /**
             * Walks the given assembly through the two archive files and returns it.
             * If and only if the assembly throws an I/O exception, the assembler stops the visit and passes it on to
             * the caller.
             */
            Assembly walkAndReturn(final Assembly assembly) throws Exception {
                for (final ArchiveEntry entry1 : from()) {
                    if (entry1.isDirectory()) {
                        continue;
                    }
                    final Optional<ArchiveEntry> entry2 = to().entry(entry1.getName());
                    final ArchiveEntrySource source1 = new ArchiveEntrySource(entry1, from());
                    if (entry2.isPresent()) {
                        assembly.visitEntriesInBothFiles(source1, new ArchiveEntrySource(entry2.get(), to()));
                    } else {
                        assembly.visitEntryInFirstFile(source1);
                    }
                }

                for (final ArchiveEntry entry2 : to()) {
                    if (entry2.isDirectory()) {
                        continue;
                    }
                    final Optional<ArchiveEntry> entry1 = from().entry(entry2.getName());
                    if (!entry1.isPresent()) {
                        assembly.visitEntryInSecondFile(new ArchiveEntrySource(entry2, to()));
                    }
                }

                return assembly;
            }
        }

        /**
         * A visitor of two archive files.
         * Note that the order of the calls to the visitor methods is undefined, so you should not depend on the
         * behavior of the current implementation in order to ensure compatibility with future versions.
         */
        private class Assembly {

            private final Map<String, EntryNameAndTwoDigests> changed = new TreeMap<>();

            private final Map<String, EntryNameAndDigest>
                    unchanged = new TreeMap<>(),
                    added = new TreeMap<>(),
                    removed = new TreeMap<>();

            DeltaModel deltaModel() {
                return DeltaModel
                        .builder()
                        .messageDigest(digest())
                        .changedEntries(changed.values())
                        .unchangedEntries(unchanged.values())
                        .addedEntries(added.values())
                        .removedEntries(removed.values())
                        .build();
            }

            /**
             * Visits a pair of archive entries with equal names in the first and second archive file.
             *
             * @param source1 the archive entry in the first archive file.
             * @param source2 the archive entry in the second archive file.
             */
            void visitEntriesInBothFiles(final ArchiveEntrySource source1, final ArchiveEntrySource source2) throws Exception {
                final String name1 = source1.name();
                assert name1.equals(source2.name());
                final String digest1 = digestValueOf(source1);
                final String digest2 = digestValueOf(source2);
                if (digest1.equals(digest2)) {
                    unchanged.put(name1, new EntryNameAndDigest(name1, digest1));
                } else {
                    changed.put(name1, new EntryNameAndTwoDigests(name1, digest1, digest2));
                }
            }

            /**
             * Visits an archive entry which is present in the first archive file, but not in the second archive file.
             *
             * @param source1 the archive entry in the first archive file.
             */
            void visitEntryInFirstFile(final ArchiveEntrySource source1) throws Exception {
                final String name = source1.name();
                removed.put(name, new EntryNameAndDigest(name, digestValueOf(source1)));
            }

            /**
             * Visits an archive entry which is present in the second archive file, but not in the first archive file.
             *
             * @param source2 the archive entry in the second archive file.
             */
            void visitEntryInSecondFile(final ArchiveEntrySource source2) throws Exception {
                final String name = source2.name();
                added.put(name, new EntryNameAndDigest(name, digestValueOf(source2)));
            }

            String digestValueOf(Source source) throws Exception {
                final MessageDigest digest = digest();
                MessageDigests.updateDigestFrom(digest, source);
                return MessageDigests.valueOf(digest);
            }
        }
    }
}
