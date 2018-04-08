package global.namespace.archive.diff;

import global.namespace.archive.api.ArchiveFileSink;
import global.namespace.archive.api.ArchiveFileSource;
import global.namespace.archive.diff.model.DeltaModel;

import java.security.MessageDigest;
import java.util.Optional;

import static java.util.Optional.empty;

/**
 * A builder for an archive file diff.
 * The default message digest is SHA-1.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "ConstantConditions"})
public class ArchiveFileDiffBuilder {

    private Optional<MessageDigest> digest = empty();

    private Optional<ArchiveFileSource<?>> first = empty(), second = empty();

    ArchiveFileDiffBuilder() { }

    /** Returns this archive file diff builder with the given message digest. */
    public ArchiveFileDiffBuilder digest(final MessageDigest digest) {
        this.digest = Optional.of(digest);
        return this;
    }

    /** Returns this archive file diff builder with the given source for reading the first archive file. */
    public ArchiveFileDiffBuilder first(final ArchiveFileSource<?> first) {
        this.first = Optional.of(first);
        return this;
    }

    /** Returns this archive file diff builder with the given source for reading the second archive file. */
    public ArchiveFileDiffBuilder second(final ArchiveFileSource<?> second) {
        this.second = Optional.of(second);
        return this;
    }

    /** Writes the delta archive file computed from the first and second archive file to the given sink. */
    @SuppressWarnings("unchecked")
    public void to(ArchiveFileSink<?> delta) throws Exception { build().to(delta); }

    /** Returns the delta model computed from the first and second archive file. */
    public DeltaModel deltaModel() throws Exception { return build().deltaModel(); }

    private ArchiveFileDiff build() {
        return create(digest.orElseGet(MessageDigests::sha1), first.get(), second.get());
    }

    private static ArchiveFileDiff create(MessageDigest digest,
                                          ArchiveFileSource<?> firstSource,
                                          ArchiveFileSource<?> secondSource) {
        return new ArchiveFileDiff() {

            MessageDigest digest() { return digest; }

            ArchiveFileSource<?> firstSource() { return firstSource; }

            ArchiveFileSource<?> secondSource() { return secondSource; }
        };
    }
}
