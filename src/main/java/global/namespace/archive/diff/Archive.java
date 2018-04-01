/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import global.namespace.archive.diff.model.DeltaModel;
import global.namespace.archive.diff.model.dto.DeltaModelAdapter;
import global.namespace.archive.diff.model.dto.DeltaModelDTO;
import global.namespace.archive.diff.spi.ArchiveFileInput;
import global.namespace.archive.diff.spi.ArchiveFileOutput;
import global.namespace.archive.diff.spi.ArchiveFileStore;
import global.namespace.fun.io.api.Codec;
import global.namespace.fun.io.api.Sink;
import global.namespace.fun.io.api.Socket;
import global.namespace.fun.io.api.Source;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import javax.xml.bind.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static global.namespace.fun.io.jaxb.JAXB.xmlCodec;
import static java.util.Objects.requireNonNull;

/**
 * Provides access to archive files and diff and patch ZIP based archive files.
 *
 * @author Christian Schlichtherle
 */
public class Archive {

    private Archive() { }

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

    static ArchiveEntrySource entrySource(ArchiveEntry entry, ArchiveFileInput input) {
        return new ArchiveEntrySource() {

            public String name() { return entry.getName(); }

            public Socket<InputStream> input() { return input.input(entry); }
        };
    }

    static ArchiveEntrySink entrySink(ArchiveEntry entry, ArchiveFileOutput output) {
        return new ArchiveEntrySink() {

            public String name() { return entry.getName(); }

            public Socket<OutputStream> output() { return output.output(entry); }
        };
    }

    static void encodeToXml(DeltaModel model, Sink sink ) throws Exception {
        jaxbCodec().encoder(sink).encode(new DeltaModelAdapter().marshal(model));
    }

    static DeltaModel decodeFromXml(Source source) throws Exception {
        return new DeltaModelAdapter().unmarshal(jaxbCodec().decoder(source).decode(DeltaModelDTO.class));
    }

    private static Codec jaxbCodec() throws JAXBException {
        return xmlCodec(jaxbContext(), Archive::modifyMarshaller, Archive::unmarshallerModifier);
    }

    private static JAXBContext jaxbContext() throws JAXBException { return JAXBContext.newInstance(DeltaModelDTO.class); }

    private static void modifyMarshaller(Marshaller m) throws PropertyException {
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }

    private static void unmarshallerModifier(Unmarshaller u) {
    }
}
