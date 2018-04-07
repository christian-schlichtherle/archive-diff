/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.it

import java.io.File

import global.namespace.archive.api.ArchiveFileStore
import global.namespace.archive.commons.compress.CommonsCompress._
import global.namespace.archive.diff.it.ArchiveITContext._
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry

/** @author Christian Schlichtherle */
trait ArchiveITContext {

  final lazy val Test1Jar: ArchiveFileStore[ZipArchiveEntry] = jar(resourceFile("test1.jar"))

  final lazy val Test2Jar: ArchiveFileStore[ZipArchiveEntry] = jar(resourceFile("test2.jar"))
}

/** @author Christian Schlichtherle */
private object ArchiveITContext {

  def resourceFile(resourceName: String): File = new File((classOf[ArchiveITContext] getResource resourceName).toURI)
}
