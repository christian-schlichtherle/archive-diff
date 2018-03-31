package global.namespace.archive.diff;

import global.namespace.archive.diff.spi.ArchiveFileInput;
import global.namespace.archive.diff.spi.ArchiveFileSink;
import global.namespace.archive.diff.spi.ArchiveFileSource;
import global.namespace.fun.io.api.function.XConsumer;

import java.util.Optional;

import static java.util.Optional.empty;

/** A builder for an archive file patch. */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "ConstantConditions"})
public class ArchiveFilePatchBuilder {

    private Optional<ArchiveFileSource> first = empty(), delta = empty();

    ArchiveFilePatchBuilder() { }

    public ArchiveFilePatchBuilder first(final ArchiveFileSource first) {
        this.first = Optional.of(first);
        return this;
    }

    public ArchiveFilePatchBuilder delta(final ArchiveFileSource delta) {
        this.delta = Optional.of(delta);
        return this;
    }

    /** Writes the second archive file computed from the first and delta archive file to the given sink. */
    public void to(ArchiveFileSink second) throws Exception { build().to(second); }

    private ArchiveFilePatch build() { return create(first.get(), delta.get()); }

    private static ArchiveFilePatch create(ArchiveFileSource firstSource, ArchiveFileSource deltaSource) {
        return new ArchiveFilePatch() {

            @Override
            void accept(final XConsumer<Engine> consumer) throws Exception {
                firstSource.acceptReader(firstInput -> deltaSource.acceptReader(deltaInput -> consumer.accept(
                        new Engine() {

                            ArchiveFileInput firstInput() { return firstInput; }

                            ArchiveFileInput deltaInput() { return deltaInput; }
                        }
                )));
            }
        };
    }
}
