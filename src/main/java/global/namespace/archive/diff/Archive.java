/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import global.namespace.archive.diff.dto.DeltaModelDTO;
import global.namespace.archive.diff.model.DeltaModel;
import global.namespace.archive.diff.spi.ArchiveFileInput;
import global.namespace.archive.diff.spi.ArchiveFileOutput;
import global.namespace.archive.diff.spi.ArchiveFileStore;
import global.namespace.fun.io.api.Codec;
import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Socket;
import global.namespace.fun.io.api.Source;
import global.namespace.fun.io.jackson.Jackson;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

/**
 * Provides access to archive files and diff and patch ZIP based archive files.
 *
 * @author Christian Schlichtherle
 */
public class Archive {

    private Archive() { }

    /**
     * The name of the entry which contains the serialized delta model in a delta-archive file.
     * This should be the first entry in the delta-archive file.
     */
    private static final String ENTRY_NAME = "META-INF/delta.json";

    /** Returns an archive file store for the given JAR file. */
    public static ArchiveFileStore jar(final File file) {
        requireNonNull(file);
        return new ArchiveFileStore() {

            @Override
            public Socket<ArchiveFileInput> input() { return () -> new ZipFileAdapter(new ZipFile(file)); }

            @Override
            public Socket<ArchiveFileOutput> output() {
                return () -> new JarOutputStreamAdapter(new JarArchiveOutputStream(new FileOutputStream(file)));
            }
        };
    }

    /** Returns an archive file store for the given ZIP file. */
    public static ArchiveFileStore zip(final File file) {
        requireNonNull(file);
        return new ArchiveFileStore() {

            @Override
            public Socket<ArchiveFileInput> input() { return () -> new ZipFileAdapter(new ZipFile(file)); }

            @Override
            public Socket<ArchiveFileOutput> output() {
                return () -> new ZipOutputStreamAdapter(new ZipArchiveOutputStream(file));
            }
        };
    }

    /**
     * Returns a builder for comparing a first archive file to a second archive file and generating a delta archive file.
     */
    public static ArchiveFileDiffBuilder diff() { return new ArchiveFileDiffBuilder(); }

    /** Returns a builder for patching the first archive file to a second archive file using a delta archive file. */
    public static ArchiveFilePatchBuilder patch() { return new ArchiveFilePatchBuilder(); }

    static ArchiveEntrySink entrySink(ArchiveFileOutput output, ArchiveEntry entry) {
        return new ArchiveEntrySink() {

            public String name() { return entry.getName(); }

            public Socket<OutputStream> output() { return output.output(entry); }
        };
    }

    static ArchiveEntrySource entrySource(ArchiveFileInput input, ArchiveEntry entry) {
        return new ArchiveEntrySource() {

            public String name() { return entry.getName(); }

            public Socket<InputStream> input() { return input.input(entry); }
        };
    }

    static void encode(ArchiveFileOutput output, DeltaModel model) throws Exception {
        encode(entrySink(output, output.entry(ENTRY_NAME)), model);
    }

    static void encode(Sink sink, DeltaModel model) throws Exception {
        encodeDTO(sink, DeltaModelDtoAdapter.marshal(model));
    }

    private static void encodeDTO(Sink sink, DeltaModelDTO dto) throws Exception {
        jsonCodec().encoder(sink).encode(dto);
    }

    static DeltaModel decode(ArchiveFileInput input) throws Exception {
        return decode(entrySource(input, input.entry(ENTRY_NAME).orElseThrow(() ->
                new InvalidDeltaArchiveFileException(new MissingArchiveEntryException(ENTRY_NAME)))));
    }

    static DeltaModel decode(Source source) throws Exception {
        return DeltaModelDtoAdapter.unmarshal(decodeDTO(source));
    }

    private static DeltaModelDTO decodeDTO(Source source) throws Exception {
        return jsonCodec().decoder(source).decode(DeltaModelDTO.class);
    }

    private static Codec jsonCodec() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        return Jackson.jsonCodec(mapper);
    }
}
