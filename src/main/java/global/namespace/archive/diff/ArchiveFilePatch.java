/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import global.namespace.archive.api.*;
import global.namespace.archive.diff.model.DeltaModel;
import global.namespace.archive.diff.model.EntryNameAndDigestValue;
import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Socket;
import global.namespace.fun.io.api.function.XConsumer;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;

import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Optional;

import static global.namespace.fun.io.bios.BIOS.copy;

/**
 * Patches a first archive file to a second archive file using a delta archive file.
 *
 * @author Christian Schlichtherle
 */
abstract class ArchiveFilePatch<F, D, S> {

    abstract ArchiveFileSource<F> firstSource();

    abstract ArchiveFileSource<D> deltaSource();

    void to(ArchiveFileSink<S> second) throws Exception {
        accept(engine -> second.acceptWriter(engine::to));
    }

    private void accept(final XConsumer<Engine> consumer) throws Exception {
        firstSource().acceptReader(firstInput -> deltaSource().acceptReader(deltaInput -> consumer.accept(
                new Engine() {

                    ArchiveFileInput<F> firstInput() { return firstInput; }

                    ArchiveFileInput<D> deltaInput() { return deltaInput; }
                }
        )));
    }

    private abstract class Engine {

        DeltaModel model;

        abstract ArchiveFileInput<F> firstInput();

        abstract ArchiveFileInput<D> deltaInput();

        void to(final ArchiveFileOutput<S> secondOutput) throws Exception {
            for (EntryNameFilter filter : passFilters(secondOutput)) {
                to(secondOutput, new NoDirectoryEntryNameFilter(filter));
            }
        }

        /**
         * Returns a list of filters for the different passes required to process the to-archive file.
         * At least one filter is required to output anything.
         * The filters should properly partition the set of entry sources, i.e. each entry source should be accepted by
         * exactly one filter.
         */
        EntryNameFilter[] passFilters(final ArchiveFileOutput<S> secondOutput) {
            if (secondOutput.sink("").entry() instanceof JarArchiveEntry) {
                // java.util.JarInputStream assumes that the file entry
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

        void to(final ArchiveFileOutput<S> secondOutput, final EntryNameFilter filter) throws Exception {

            class MyArchiveEntrySink implements Sink {

                private final EntryNameAndDigestValue entryNameAndDigest;

                MyArchiveEntrySink(final EntryNameAndDigestValue entryNameAndDigest) {
                    assert null != entryNameAndDigest;
                    this.entryNameAndDigest = entryNameAndDigest;
                }

                @Override
                public Socket<OutputStream> output() {
                    return secondOutput.sink(entryNameAndDigest.entryName()).output().map(out -> {
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

            }

            abstract class Patch<E> {

                abstract ArchiveFileInput<E> input();

                abstract IOException ioException(Throwable cause);

                final <T> void apply(final Transformation<T> transformation, final Iterable<T> iterable)
                        throws Exception {
                    for (final T item : iterable) {
                        final EntryNameAndDigestValue entryNameAndDigestValue = transformation.apply(item);
                        final String name = entryNameAndDigestValue.entryName();
                        if (!filter.accept(name)) {
                            continue;
                        }
                        final Optional<ArchiveEntrySource<E>> entry = input().source(name);
                        try {
                            copy(
                                    entry.orElseThrow(() -> ioException(new MissingArchiveEntryException(name))),
                                    new MyArchiveEntrySink(entryNameAndDigestValue)
                            );
                        } catch (WrongMessageDigestException e) {
                            throw ioException(e);
                        }
                    }
                }
            }

            class FirstArchiveFilePatch extends Patch<F> {

                @Override
                ArchiveFileInput<F> input() { return firstInput(); }

                @Override
                IOException ioException(Throwable cause) { return new WrongFirstArchiveFileException(cause); }
            }

            class DeltaArchiveFilePatch extends Patch<D> {

                @Override
                ArchiveFileInput<D> input() { return deltaInput(); }

                @Override
                IOException ioException(Throwable cause) { return new InvalidDeltaArchiveFileException(cause); }
            }

            // Order is important here!
            new FirstArchiveFilePatch().apply(new IdentityTransformation(), model().unchangedEntries());
            new DeltaArchiveFilePatch().apply(new EntryNameAndDigest2Transformation(), model().changedEntries());
            new DeltaArchiveFilePatch().apply(new IdentityTransformation(), model().addedEntries());
        }

        MessageDigest digest() throws Exception {
            return MessageDigest.getInstance(model().digestAlgorithmName());
        }

        DeltaModel model() throws Exception {
            final DeltaModel model = this.model;
            return null != model ? model : (this.model = Archive.decode(deltaInput()));
        }
    }
}
