/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.io.delta;

import global.namespace.archive.io.api.*;
import global.namespace.archive.io.delta.model.DeltaModel;
import global.namespace.archive.io.delta.model.EntryNameAndDigestValue;
import global.namespace.archive.io.delta.model.EntryNameAndTwoDigestValues;
import global.namespace.fun.io.api.Source;
import global.namespace.fun.io.api.function.XFunction;

import java.security.MessageDigest;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static global.namespace.archive.io.delta.Delta.encodeModel;
import static global.namespace.archive.io.delta.MessageDigests.updateDigestFrom;
import static global.namespace.archive.io.delta.MessageDigests.valueOf;

/**
 * Compares a first archive file to a second archive file and generates a delta archive file.
 *
 * @author Christian Schlichtherle
 */
abstract class ArchiveFileDiff<F, S, D> {

    abstract MessageDigest digest();

    abstract ArchiveFileSource<F> firstSource();

    abstract ArchiveFileSource<S> secondSource();

    void to(ArchiveFileSink<D> delta) throws Exception {
        apply(engine -> {
            delta.acceptWriter(engine::to);
            return null;
        });
    }

    DeltaModel deltaModel() throws Exception { return apply(Engine::deltaModel); }

    private <T> T apply(XFunction<Engine, T> function) throws Exception {
        return firstSource().applyReader(firstInput -> secondSource().applyReader(secondInput -> function.apply(
                new Engine() {

                    ArchiveFileInput<F> firstInput() { return firstInput; }

                    ArchiveFileInput<S> secondInput() { return secondInput; }
                }
        )));
    }

    private abstract class Engine {

        abstract ArchiveFileInput<F> firstInput();

        abstract ArchiveFileInput<S> secondInput();

        void to(final ArchiveFileOutput<D> deltaOutput) throws Exception {

            final class Streamer {

                private final DeltaModel model = deltaModel();

                private Streamer() throws Exception { encodeModel(deltaOutput, model); }

                private void stream() throws Exception {
                    for (final ArchiveEntrySource<S> secondEntry : secondInput()) {
                        final String name = secondEntry.name();
                        if (changedOrAdded(name)) {
                            secondEntry.copyTo(deltaOutput.sink(name));
                        }
                    }
                }

                private boolean changedOrAdded(String name) {
                    return null != model.changed(name) || null != model.added(name);
                }
            }

            new Streamer().stream();
        }

        DeltaModel deltaModel() throws Exception { return new Assembler().walkAndReturn(new Assembly()).deltaModel(); }

        class Assembler {

            /**
             * Walks the given assembly through the two archive files and returns it.
             * If and only if the assembly throws an I/O exception, the assembler stops the visit and passes it on to
             * the caller.
             */
            Assembly walkAndReturn(final Assembly assembly) throws Exception {
                for (final ArchiveEntrySource<F> firstEntry : firstInput()) {
                    if (firstEntry.isDirectory()) {
                        continue;
                    }
                    final Optional<ArchiveEntrySource<S>> secondEntry = secondInput().source(firstEntry.name());
                    if (secondEntry.isPresent()) {
                        assembly.visitEntriesInBothFiles(firstEntry, secondEntry.get());
                    } else {
                        assembly.visitEntryInFirstFile(firstEntry);
                    }
                }

                for (final ArchiveEntrySource<S> secondEntry : secondInput()) {
                    if (secondEntry.isDirectory()) {
                        continue;
                    }
                    final Optional<ArchiveEntrySource<F>> firstEntry = firstInput().source(secondEntry.name());
                    if (!firstEntry.isPresent()) {
                        assembly.visitEntryInSecondFile(secondEntry);
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
        class Assembly {

            final Map<String, EntryNameAndTwoDigestValues> changed = new TreeMap<>();

            final Map<String, EntryNameAndDigestValue>
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
             * @param firstSource the source for reading the archive entry in the first archive file.
             * @param secondSource the source for reading the archive entry in the second archive file.
             */
            void visitEntriesInBothFiles(final ArchiveEntrySource<F> firstSource,
                                         final ArchiveEntrySource<S> secondSource)
                    throws Exception {
                final String firstName = firstSource.name();
                assert firstName.equals(secondSource.name());
                final String firstValue = digestValueOf(firstSource);
                final String secondValue = digestValueOf(secondSource);
                if (firstValue.equals(secondValue)) {
                    unchanged.put(firstName, new EntryNameAndDigestValue(firstName, firstValue));
                } else {
                    changed.put(firstName, new EntryNameAndTwoDigestValues(firstName, firstValue, secondValue));
                }
            }

            /**
             * Visits an archive entry which is present in the first archive file, but not in the second archive file.
             *
             * @param firstSource the source for reading the archive entry in the first archive file.
             */
            void visitEntryInFirstFile(final ArchiveEntrySource<F> firstSource) throws Exception {
                final String firstName = firstSource.name();
                removed.put(firstName, new EntryNameAndDigestValue(firstName, digestValueOf(firstSource)));
            }

            /**
             * Visits an archive entry which is present in the second archive file, but not in the first archive file.
             *
             * @param secondSource the source for reading the archive entry in the second archive file.
             */
            void visitEntryInSecondFile(final ArchiveEntrySource<S> secondSource) throws Exception {
                final String secondName = secondSource.name();
                added.put(secondName, new EntryNameAndDigestValue(secondName, digestValueOf(secondSource)));
            }

            String digestValueOf(final Source source) throws Exception {
                final MessageDigest digest = digest();
                digest.reset();
                updateDigestFrom(digest, source);
                return valueOf(digest);
            }
        }
    }
}
