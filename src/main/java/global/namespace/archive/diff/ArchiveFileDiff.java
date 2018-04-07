/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import global.namespace.archive.api.*;
import global.namespace.archive.diff.model.DeltaModel;
import global.namespace.archive.diff.model.EntryNameAndDigestValue;
import global.namespace.archive.diff.model.EntryNameAndTwoDigestValues;
import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Source;
import global.namespace.fun.io.api.function.XFunction;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.security.MessageDigest;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static global.namespace.archive.diff.Archive.*;
import static global.namespace.fun.io.bios.BIOS.copy;

/**
 * Compares a first archive file to a second archive file and generates a delta archive file.
 *
 * @author Christian Schlichtherle
 */
abstract class ArchiveFileDiff<F, S, D> {

    private static final Pattern COMPRESSED_FILE_EXTENSIONS =
            Pattern.compile(".*\\.(ear|jar|war|zip|gz|xz)", Pattern.CASE_INSENSITIVE);

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

                private Streamer() throws Exception { encode(deltaOutput, model); }

                private void stream() throws Exception {
                    for (final ArchiveFileEntry<S> secondEntry : secondInput()) {
                        final String name = secondEntry.name();
                        if (changedOrAdded(name)) {
                            final ArchiveFileEntry<D> deltaEntry = deltaEntry(name);
                            if (secondEntry.entry() instanceof ZipArchiveEntry && deltaEntry.entry() instanceof ZipArchiveEntry &&
                                    COMPRESSED_FILE_EXTENSIONS.matcher(name).matches()) {
                                final ZipArchiveEntry secondZipEntry = (ZipArchiveEntry) secondEntry.entry();
                                final ZipArchiveEntry deltaZipEntry = (ZipArchiveEntry) deltaEntry.entry();
                                final long size = secondZipEntry.getSize();

                                deltaZipEntry.setMethod(ZipArchiveOutputStream.STORED);
                                deltaZipEntry.setSize(size);
                                deltaZipEntry.setCompressedSize(size);
                                deltaZipEntry.setCrc(secondZipEntry.getCrc());
                            }
                            copy(secondSource(secondEntry), deltaSink(deltaEntry));
                        }
                    }
                }

                private Source secondSource(ArchiveFileEntry<S> secondEntry) {
                    return entrySource(secondInput(), secondEntry);
                }

                private Sink deltaSink(ArchiveFileEntry<D> deltaEntry) { return entrySink(deltaOutput, deltaEntry); }

                private ArchiveFileEntry<D> deltaEntry(String name) { return deltaOutput.entry(name); }

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
                for (final ArchiveFileEntry<F> firstEntry : firstInput()) {
                    if (firstEntry.isDirectory()) {
                        continue;
                    }
                    final Optional<ArchiveFileEntry<S>> secondEntry = secondInput().entry(firstEntry.name());
                    final ArchiveEntrySource firstSource = entrySource(firstInput(), firstEntry);
                    if (secondEntry.isPresent()) {
                        final ArchiveEntrySource secondSource = entrySource(secondInput(), secondEntry.get());
                        assembly.visitEntriesInBothFiles(firstSource, secondSource);
                    } else {
                        assembly.visitEntryInFirstFile(firstSource);
                    }
                }

                for (final ArchiveFileEntry<S> secondEntry : secondInput()) {
                    if (secondEntry.isDirectory()) {
                        continue;
                    }
                    final Optional<ArchiveFileEntry<F>> firstEntry = firstInput().entry(secondEntry.name());
                    if (!firstEntry.isPresent()) {
                        final ArchiveEntrySource secondSource = entrySource(secondInput(), secondEntry);
                        assembly.visitEntryInSecondFile(secondSource);
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
            void visitEntriesInBothFiles(final ArchiveEntrySource firstSource, final ArchiveEntrySource secondSource)
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
            void visitEntryInFirstFile(final ArchiveEntrySource firstSource) throws Exception {
                final String firstName = firstSource.name();
                removed.put(firstName, new EntryNameAndDigestValue(firstName, digestValueOf(firstSource)));
            }

            /**
             * Visits an archive entry which is present in the second archive file, but not in the first archive file.
             *
             * @param secondSource the source for reading the archive entry in the second archive file.
             */
            void visitEntryInSecondFile(final ArchiveEntrySource secondSource) throws Exception {
                final String secondName = secondSource.name();
                added.put(secondName, new EntryNameAndDigestValue(secondName, digestValueOf(secondSource)));
            }

            String digestValueOf(final Source source) throws Exception {
                final MessageDigest digest = digest();
                digest.reset();
                MessageDigests.updateDigestFrom(digest, source);
                return MessageDigests.valueOf(digest);
            }
        }
    }
}
