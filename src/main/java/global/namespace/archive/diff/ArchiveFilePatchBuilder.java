package global.namespace.archive.diff;

import global.namespace.archive.api.ArchiveFileSink;
import global.namespace.archive.api.ArchiveFileSource;

import java.util.Optional;

import static java.util.Optional.empty;

/**
 * A builder for an archive file patch.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "ConstantConditions"})
public class ArchiveFilePatchBuilder {

    private Optional<ArchiveFileSource<?>> first = empty(), delta = empty();

    ArchiveFilePatchBuilder() { }

    /** Returns this archive file patch builder with the given source for reading the first archive file. */
    public ArchiveFilePatchBuilder first(final ArchiveFileSource<?> first) {
        this.first = Optional.of(first);
        return this;
    }

    /** Returns this archive file patch builder with the given source for reading the delta archive file. */
    public ArchiveFilePatchBuilder delta(final ArchiveFileSource<?> delta) {
        this.delta = Optional.of(delta);
        return this;
    }

    /** Writes the second archive file computed from the first and delta archive file to the given sink. */
    public void to(ArchiveFileSink<?> second) throws Exception { build().to(second); }

    private ArchiveFilePatch build() { return create(first.get(), delta.get()); }

    private static ArchiveFilePatch create(ArchiveFileSource<?> firstSource, ArchiveFileSource<?> deltaSource) {
        return new ArchiveFilePatch() {

            ArchiveFileSource<?> firstSource() { return firstSource; }

            ArchiveFileSource<?> deltaSource() { return deltaSource; }
        };
    }
}
