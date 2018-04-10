/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.io.commons.compress;

import global.namespace.archive.io.api.ArchiveEntrySink;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

/**
 * Supports writing the content of an underlying ZIP archive entry.
 *
 * @author Christian Schlichtherle
 */
abstract class ZipArchiveEntrySink extends ArchiveEntrySink<ZipArchiveEntry> {

    abstract void copyFrom(ZipArchiveEntrySource source) throws Exception;
}
