/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.patch;

import global.namespace.archive.diff.io.*;
import global.namespace.archive.diff.model.DeltaModel;
import global.namespace.archive.diff.model.EntryNameAndDigest;
import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Socket;

import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;

import static java.util.Optional.empty;

/**
 * Applies a <it>delta-archive file</it> to a <it>from-archive file</it> and generates a <it>to-archive file</it>.
 *
 * @author Christian Schlichtherle
 */
public abstract class ArchiveFilePatch {

    /** Returns a new builder for an archive file patch. */
    public static Builder builder() { return new Builder(); }

    /** Writes the output to the given to-archive file. */
    public abstract void outputTo(ArchiveFileSink to) throws Exception;

    /** A builder for an archive file patch. */
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "ConstantConditions"})
    public static class Builder {

        private Optional<ArchiveFileSource> from = empty(), delta = empty();

        private Builder() { }

        public Builder from(final ArchiveFileSource from) {
            this.from = Optional.of(from);
            return this;
        }

        public Builder delta(final ArchiveFileSource delta) {
            this.delta = Optional.of(delta);
            return this;
        }

        public ArchiveFilePatch build() { return create(from.get(), delta.get()); }

        private static ArchiveFilePatch create(final ArchiveFileSource fromSource, final ArchiveFileSource deltaSource) {
            return new ArchiveFilePatch() {

                @Override
                public void outputTo(final ArchiveFileSink toSink) throws Exception {
                    fromSource.acceptReader(from ->
                            deltaSource.acceptReader(delta ->
                                    toSink.acceptWriter(to ->
                                            new Engine() {

                                                public ArchiveFileInput from() { return from; }

                                                public ArchiveFileInput delta() { return delta; }
                                            }.outputTo(to)
                                    )
                            )
                    );
                }
            };
        }
    }

    public abstract static class Engine {

        private volatile DeltaModel model;

        /** Returns the from-archive file. */
        protected abstract @WillNotClose
        ArchiveFileInput from();

        /** Returns the delta-archive file. */
        protected abstract @WillNotClose
        ArchiveFileInput delta();

        /** Writes the output to the given to-archive file. */
        public void outputTo(final @WillNotClose ArchiveFileOutput to) throws Exception {
            for (EntryNameFilter filter : passFilters(to)) {
                outputTo(to, new NoDirectoryEntryNameFilter(filter));
            }
        }

        /**
         * Returns a list of filters for the different passes required to process the to-archive file.
         * At least one filter is required to output anything.
         * The filters should properly partition the set of entry sources, i.e. each entry source should be accepted by
         * exactly one filter.
         */
        private EntryNameFilter[] passFilters(final @WillNotClose ArchiveFileOutput update) {
            if (update.entry("") instanceof JarEntry) {
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

        private void outputTo(final @WillNotClose ArchiveFileOutput to, final EntryNameFilter filter) throws Exception {

            class ArchiveEntrySink implements Sink {

                final EntryNameAndDigest entryNameAndDigest;

                ArchiveEntrySink(final EntryNameAndDigest entryNameAndDigest) {
                    assert null != entryNameAndDigest;
                    this.entryNameAndDigest = entryNameAndDigest;
                }

                @Override
                public Socket<OutputStream> output() {
                    final ZipEntry entry = entry(entryNameAndDigest.name());
                    return output(entry).map(out ->
                        new DigestOutputStream(out, digest()) {

                            @Override
                            public void close() throws IOException {
                                super.close();
                                if (!valueOfDigest().equals(entryNameAndDigest.digest())) {
                                    throw new WrongMessageDigestException(entryNameAndDigest.name());
                                }
                            }

                            String valueOfDigest() { return MessageDigests.valueOf(digest); }
                        }
                    );
                }

                ZipEntry entry(String name) { return to.entry(name); }

                Socket<OutputStream> output(ZipEntry entry) { return to.output(entry); }
            }

            abstract class Patch {

                abstract ArchiveFileInput input();

                abstract IOException ioException(Throwable cause);

                final <T> void apply(final Transformation<T> transformation, final Iterable<T> iterable) throws Exception {
                    for (final T item : iterable) {
                        final EntryNameAndDigest entryNameAndDigest = transformation.apply(item);
                        final String name = entryNameAndDigest.name();
                        if (!filter.accept(name)) {
                            continue;
                        }
                        final Optional<ZipEntry> entry = input().entry(name);
                        try {
                            Copy.copy(
                                    new ArchiveEntrySource(entry.orElseThrow(() -> ioException(new MissingArchiveEntryException(name))), input()),
                                    new ArchiveEntrySink(entryNameAndDigest)
                            );
                        } catch (WrongMessageDigestException e) {
                            throw ioException(e);
                        }
                    }
                }
            }

            class FromArchiveFilePatch extends Patch {

                @Override
                ArchiveFileInput input() { return from(); }

                @Override
                IOException ioException(Throwable cause) { return new WrongFromArchiveFileException(cause); }
            }

            class DeltaArchivePatch extends Patch {

                @Override
                ArchiveFileInput input() { return delta(); }

                @Override
                IOException ioException(Throwable cause) { return new InvalidDeltaArchiveFileException(cause); }
            }

            // Order is important here!
            new FromArchiveFilePatch().apply(new IdentityTransformation(), model().unchangedEntries());
            new DeltaArchivePatch().apply(new EntryNameAndDigest2Transformation(), model().changedEntries());
            new DeltaArchivePatch().apply(new IdentityTransformation(), model().addedEntries());
        }

        private MessageDigest digest() throws Exception {
            return MessageDigests.create(model().digestAlgorithmName());
        }

        private DeltaModel model() throws Exception {
            final DeltaModel model = this.model;
            return null != model ? model : (this.model = loadModel());
        }

        private DeltaModel loadModel() throws Exception {
            return DeltaModel.decodeFromXml(new ArchiveEntrySource(modelArchiveEntry(), delta()));
        }

        private ZipEntry modelArchiveEntry() throws Exception {
            final String name = DeltaModel.ENTRY_NAME;
            return delta().entry(name).orElseThrow(() -> new InvalidDeltaArchiveFileException(new MissingArchiveEntryException(name)));
        }
    }
}
