/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.patch;

import global.namespace.archive.diff.io.*;
import global.namespace.archive.diff.model.DeltaModel;
import global.namespace.archive.diff.model.EntryNameAndDigestValue;
import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Socket;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;

import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Optional;

import static java.util.Optional.empty;

/**
 * Patches a first archive file to a second archive file using a delta archive file.
 *
 * @author Christian Schlichtherle
 */
public abstract class ArchiveFilePatch {

    /** Returns a new builder for an archive file patch. */
    public static Builder builder() { return new Builder(); }

    /** Writes the output to the given second-archive file. */
    public abstract void patchTo(ArchiveFileSink second) throws Exception;

    /** A builder for an archive file patch. */
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "ConstantConditions"})
    public static class Builder {

        private Optional<ArchiveFileSource> first = empty(), delta = empty();

        private Builder() { }

        public Builder first(final ArchiveFileSource first) {
            this.first = Optional.of(first);
            return this;
        }

        public Builder delta(final ArchiveFileSource delta) {
            this.delta = Optional.of(delta);
            return this;
        }

        public ArchiveFilePatch build() { return create(first.get(), delta.get()); }

        private static ArchiveFilePatch create(final ArchiveFileSource firstSource, final ArchiveFileSource deltaSource) {
            return new ArchiveFilePatch() {

                @Override
                public void patchTo(final ArchiveFileSink secondSink) throws Exception {
                    firstSource.acceptReader(firstInput ->
                            deltaSource.acceptReader(deltaInput ->
                                    secondSink.acceptWriter(secondOutput ->
                                            new Engine() {

                                                public ArchiveFileInput firstInput() { return firstInput; }

                                                public ArchiveFileInput deltaInput() { return deltaInput; }
                                            }.outputTo(secondOutput)
                                    )
                            )
                    );
                }
            };
        }
    }

    public abstract static class Engine {

        private volatile DeltaModel model;

        /** Returns the first-archive file. */
        protected abstract @WillNotClose ArchiveFileInput firstInput();

        /** Returns the delta-archive file. */
        protected abstract @WillNotClose ArchiveFileInput deltaInput();

        /** Writes the output to the given to-archive file. */
        public void outputTo(final @WillNotClose ArchiveFileOutput secondOutput) throws Exception {
            for (EntryNameFilter filter : passFilters(secondOutput)) {
                outputTo(secondOutput, new NoDirectoryEntryNameFilter(filter));
            }
        }

        /**
         * Returns a list of filters for the different passes required to process the to-archive file.
         * At least one filter is required to output anything.
         * The filters should properly partition the set of entry sources, i.e. each entry source should be accepted by
         * exactly one filter.
         */
        private EntryNameFilter[] passFilters(final @WillNotClose ArchiveFileOutput secondOutput) {
            if (secondOutput.entry("") instanceof JarArchiveEntry) {
                // The JarInputStream class assumes that the file entry
                // "META-INF/MANIFEST.MF" should either be the first or the second
                // entry (if preceded by the directory entry "META-INF/"), so we
                // need to process the delta-archive file in two passes with a
                // corresponding filter to ensure this order.
                // Note that the directory entry "META-INF/" is always part of the
                // unchanged delta set because it's content is always empty.
                // Thus, by copying the unchanged entries before the changed
                // entries, the directory entry "META-INF/" will always appear
                // before the file entry "META-INF/MANIFEST.MF".
                final EntryNameFilter manifestFilter = new ManifestEntryNameFilter();
                return new EntryNameFilter[] { manifestFilter, new InverseEntryNameFilter(manifestFilter) };
            } else {
                return new EntryNameFilter[] { new AcceptAllEntryNameFilter() };
            }
        }

        private void outputTo(final @WillNotClose ArchiveFileOutput secondOutput, final EntryNameFilter filter) throws Exception {

            class MyArchiveEntrySink implements Sink {

                private final EntryNameAndDigestValue entryNameAndDigest;

                MyArchiveEntrySink(final EntryNameAndDigestValue entryNameAndDigest) {
                    assert null != entryNameAndDigest;
                    this.entryNameAndDigest = entryNameAndDigest;
                }

                @Override
                public Socket<OutputStream> output() {
                    final ArchiveEntry secondEntry = secondEntry(entryNameAndDigest.entryName());
                    return secondSink(secondEntry).map(out -> {
                        final MessageDigest digest = digest();
                        digest.reset();
                        return new DigestOutputStream(out, digest) {

                            @Override
                            public void close() throws IOException {
                                super.close();
                                if (!valueOfDigest().equals(entryNameAndDigest.digestValue())) {
                                    throw new WrongMessageDigestException(entryNameAndDigest.entryName());
                                }
                            }

                            String valueOfDigest() { return MessageDigests.valueOf(digest); }
                        };
                    });
                }

                private ArchiveEntry secondEntry(String name) { return secondOutput.entry(name); }

                private Socket<OutputStream> secondSink(ArchiveEntry secondEntry) {
                    return secondOutput.output(secondEntry);
                }
            }

            abstract class Patch {

                abstract ArchiveFileInput input();

                abstract IOException ioException(Throwable cause);

                final <T> void apply(final Transformation<T> transformation, final Iterable<T> iterable) throws Exception {
                    for (final T item : iterable) {
                        final EntryNameAndDigestValue entryNameAndDigestValue = transformation.apply(item);
                        final String name = entryNameAndDigestValue.entryName();
                        if (!filter.accept(name)) {
                            continue;
                        }
                        final Optional<ArchiveEntry> entry = input().entry(name);
                        try {
                            Copy.copy(
                                    new ArchiveEntrySource(entry.orElseThrow(() -> ioException(new MissingArchiveEntryException(name))), input()),
                                    new MyArchiveEntrySink(entryNameAndDigestValue)
                            );
                        } catch (WrongMessageDigestException e) {
                            throw ioException(e);
                        }
                    }
                }
            }

            class FirstArchiveFilePatch extends Patch {

                @Override
                ArchiveFileInput input() { return firstInput(); }

                @Override
                IOException ioException(Throwable cause) { return new WrongFromArchiveFileException(cause); }
            }

            class DeltaArchiveFilePatch extends Patch {

                @Override
                ArchiveFileInput input() { return deltaInput(); }

                @Override
                IOException ioException(Throwable cause) { return new InvalidDeltaArchiveFileException(cause); }
            }

            // Order is important here!
            new FirstArchiveFilePatch().apply(new IdentityTransformation(), model().unchangedEntries());
            new DeltaArchiveFilePatch().apply(new EntryNameAndDigest2Transformation(), model().changedEntries());
            new DeltaArchiveFilePatch().apply(new IdentityTransformation(), model().addedEntries());
        }

        private MessageDigest digest() throws Exception {
            return MessageDigests.create(model().digestAlgorithmName());
        }

        private DeltaModel model() throws Exception {
            final DeltaModel model = this.model;
            return null != model ? model : (this.model = loadModel());
        }

        private DeltaModel loadModel() throws Exception {
            return DeltaModel.decodeFromXml(new ArchiveEntrySource(modelArchiveEntry(), deltaInput()));
        }

        private ArchiveEntry modelArchiveEntry() throws Exception {
            final String name = DeltaModel.ENTRY_NAME;
            return deltaInput().entry(name).orElseThrow(() -> new InvalidDeltaArchiveFileException(new MissingArchiveEntryException(name)));
        }
    }
}
