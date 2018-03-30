/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.fun.io.zip.zip.patch;

import global.namespace.fun.io.zip.zip.io.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * Applies a delta ZIP file to an input archive and generates an output archive.
 * Archives may be ZIP, JAR, EAR or WAR files.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class ZipPatch {

    /** Returns a new builder for a ZIP patch. */
    public static Builder builder() { return new Builder(); }

    public abstract void output(File file) throws Exception;
    public abstract void output(ZipSink sink) throws Exception;

    /** A builder for a ZIP patch. */
    public static class Builder {

        private @CheckForNull ZipSource input, delta;

        Builder() { }

        public Builder input(final @Nullable File file) {
            return input(null == file ? null : new ZipFileStore(file));
        }

        public Builder input(final @Nullable ZipSource archive) {
            this.input = archive;
            return this;
        }

        public Builder delta(final @Nullable File file) {
            return delta(null == file ? null : new ZipFileStore(file));
        }

        public Builder delta(final @Nullable ZipSource archive) {
            this.delta = archive;
            return this;
        }

        public ZipPatch build() { return create(input, delta); }

        private static ZipPatch create(
                final ZipSource input,
                final ZipSource delta) {
            requireNonNull(input);
            requireNonNull(delta);

            return new ZipPatch() {

                @Override
                public void output(File file) throws Exception {
                    output(new ZipFileStore(file));
                }

                @Override
                public void output(final ZipSink sink) throws Exception {
                    class InputTask implements ZipInputTask<Void> {
                        public Void execute(final @WillNotClose ZipInput input) throws Exception {
                            class DeltaTask implements ZipInputTask<Void> {
                                public Void execute(final @WillNotClose ZipInput delta) throws Exception {
                                    class OutputTask implements ZipOutputTask<Void> {
                                        public Void execute(final @WillNotClose ZipOutput output) throws Exception {
                                            new RawZipPatch() {
                                                protected ZipInput input() { return input; }
                                                protected ZipInput delta() { return delta; }
                                            }.output(output);
                                            return null;
                                        }
                                    } // OutputTask
                                    return ZipSinks.execute(new OutputTask()).on(sink);
                                }
                            } // DeltaTask
                            return ZipSources.execute(new DeltaTask()).on(delta);
                        }
                    } // InputTask
                    ZipSources.execute(new InputTask()).on(input);
                }
            }; // ZipPatch
        }
    } // Builder
}