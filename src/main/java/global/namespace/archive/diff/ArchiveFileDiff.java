/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import global.namespace.archive.diff.model.DeltaModel;
import global.namespace.archive.diff.model.EntryNameAndDigestValue;
import global.namespace.archive.diff.model.EntryNameAndTwoDigestValues;
import global.namespace.archive.diff.spi.ArchiveFileInput;
import global.namespace.archive.diff.spi.ArchiveFileOutput;
import global.namespace.archive.diff.spi.ArchiveFileSink;
import global.namespace.archive.diff.spi.ArchiveFileSource;
import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Source;
import global.namespace.fun.io.api.function.XFunction;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.security.MessageDigest;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static global.namespace.archive.diff.Archive.*;
import static global.namespace.archive.diff.Copy.copy;

/**
 * Compares a first archive file to a second archive file and generates a delta archive file.
 *
 * @author Christian Schlichtherle
 */
abstract class ArchiveFileDiff {

    private static final Pattern COMPRESSED_FILE_EXTENSIONS =
            Pattern.compile(".*\\.(ear|jar|war|zip|gz|xz)", Pattern.CASE_INSENSITIVE);

    abstract MessageDigest digest();

    abstract ArchiveFileSource firstSource();

    abstract ArchiveFileSource secondSource();

    void to(ArchiveFileSink delta) throws Exception {
        apply(engine -> {
            delta.acceptWriter(engine::diffTo);
            return null;
        });
    }

    DeltaModel deltaModel() throws Exception { return apply(Engine::deltaModel); }

    private <T> T apply(XFunction<Engine, T> function) throws Exception {
        return firstSource().applyReader(firstInput -> secondSource().applyReader(secondInput -> function.apply(
                new Engine() {

                    ArchiveFileInput firstInput() { return firstInput; }

                    ArchiveFileInput secondInput() { return secondInput; }
                }
        )));
    }

    private abstract class Engine {

        abstract ArchiveFileInput firstInput();

        abstract ArchiveFileInput secondInput();

        void diffTo(final ArchiveFileOutput deltaOutput) throws Exception {

            final class Streamer {

                private final DeltaModel model = deltaModel();

                private Streamer() throws Exception { encodeToXml(model, deltaSink(deltaEntry(DeltaModel.ENTRY_NAME))); }

                private void stream() throws Exception {
                    for (final ArchiveEntry secondEntry : secondInput()) {
                        final String name = secondEntry.getName();
                        if (changedOrAdded(name)) {
                            final ArchiveEntry deltaEntry = deltaEntry(name);
                            if (secondEntry instanceof ZipArchiveEntry && deltaEntry instanceof ZipArchiveEntry &&
                                    COMPRESSED_FILE_EXTENSIONS.matcher(name).matches()) {
                                final ZipArchiveEntry secondZipEntry = (ZipArchiveEntry) secondEntry;
                                final ZipArchiveEntry deltaZipEntry = (ZipArchiveEntry) deltaEntry;
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

                private Source secondSource(ArchiveEntry secondEntry) {
                    return entrySource(secondEntry, secondInput());
                }

                private Sink deltaSink(ArchiveEntry deltaEntry) {
                    return entrySink(deltaEntry, deltaOutput);
                }

                private ArchiveEntry deltaEntry(String name) { return deltaOutput.entry(name); }

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
                for (final ArchiveEntry firstEntry : firstInput()) {
                    if (firstEntry.isDirectory()) {
                        continue;
                    }
                    final Optional<ArchiveEntry> secondEntry = secondInput().entry(firstEntry.getName());
                    final ArchiveEntrySource firstSource = entrySource(firstEntry, firstInput());
                    if (secondEntry.isPresent()) {
                        final ArchiveEntrySource secondSource = entrySource(secondEntry.get(), secondInput());
                        assembly.visitEntriesInBothFiles(firstSource, secondSource);
                    } else {
                        assembly.visitEntryInFirstFile(firstSource);
                    }
                }

                for (final ArchiveEntry secondEntry : secondInput()) {
                    if (secondEntry.isDirectory()) {
                        continue;
                    }
                    final Optional<ArchiveEntry> firstEntry = firstInput().entry(secondEntry.getName());
                    if (!firstEntry.isPresent()) {
                        final ArchiveEntrySource secondSource = entrySource(secondEntry, secondInput());
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
