/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.it

import java.io.File

import global.namespace.archive.diff.Archive._
import global.namespace.archive.diff.it.ArchiveITContext._
import global.namespace.archive.diff.spi.ArchiveFileStore

/** @author Christian Schlichtherle */
trait ArchiveITContext {

  final lazy val Test1Jar: ArchiveFileStore = jar(resourceFile("test1.jar"))

  final lazy val Test2Jar: ArchiveFileStore = jar(resourceFile("test2.jar"))
}

/** @author Christian Schlichtherle */
private object ArchiveITContext {

  def resourceFile(resourceName: String): File = new File((classOf[ArchiveITContext] getResource resourceName).toURI)
}
