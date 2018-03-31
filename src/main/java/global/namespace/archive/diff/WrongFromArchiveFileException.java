/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff;

import java.io.IOException;

/**
 * Indicates that the first archive file provided for patching doesn't match the first archive file which was used to
 * compute the delta archive file.
 *
 * @author Christian Schlichtherle
 */
public class WrongFromArchiveFileException extends IOException {

    private static final long serialVersionUID = 0L;

    WrongFromArchiveFileException(Throwable cause) { super(cause); }
}
