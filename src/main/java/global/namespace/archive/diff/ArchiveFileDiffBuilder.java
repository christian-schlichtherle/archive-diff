package global.namespace.archive.diff;

import global.namespace.archive.diff.model.DeltaModel;
import global.namespace.archive.diff.spi.ArchiveFileInput;
import global.namespace.archive.diff.spi.ArchiveFileSink;
import global.namespace.archive.diff.spi.ArchiveFileSource;
import global.namespace.fun.io.api.function.XFunction;

import java.security.MessageDigest;
import java.util.Optional;

import static java.util.Optional.empty;

/**
 * A builder for an archive file diff.
 * The default message digest is SHA-1.
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "ConstantConditions"})
public class ArchiveFileDiffBuilder {

    private Optional<MessageDigest> digest = empty();

    private Optional<ArchiveFileSource> first = empty(), second = empty();

    ArchiveFileDiffBuilder() { }

    /** Returns this builder for an archive file diff with the given message digest. */
    public ArchiveFileDiffBuilder digest(final MessageDigest digest) {
        this.digest = Optional.of(digest);
        return this;
    }

    /** Returns this builder for an archive file diff with the given source for reading the first archive file. */
    public ArchiveFileDiffBuilder first(final ArchiveFileSource first) {
        this.first = Optional.of(first);
        return this;
    }

    /** Returns this builder for an archive file diff with the given source for reading the second archive file. */
    public ArchiveFileDiffBuilder second(final ArchiveFileSource second) {
        this.second = Optional.of(second);
        return this;
    }

    /** Writes the delta archive file computed from the first and second archive file to the given sink. */
    public void to(ArchiveFileSink delta) throws Exception { build().diffTo(delta); }

    /** Returns the delta model computed from the first and second archive file. */
    public DeltaModel deltaModel() throws Exception { return build().deltaModel(); }

    private ArchiveFileDiff build() {
        return create(digest.orElseGet(MessageDigests::sha1), first.get(), second.get());
    }

    private static ArchiveFileDiff create(MessageDigest digest,
                                          ArchiveFileSource firstSource,
                                          ArchiveFileSource secondSource) {
        return new ArchiveFileDiff() {

            @Override
            public <T> T apply(XFunction<Engine, T> function) throws Exception {
                return firstSource.applyReader(firstInput -> secondSource.applyReader(secondInput -> function.apply(
                        new Engine() {

                            public MessageDigest digest() { return digest; }

                            public ArchiveFileInput firstInput() { return firstInput; }

                            public ArchiveFileInput secondInput() { return secondInput; }
                        }
                )));
            }
        };
    }
}
