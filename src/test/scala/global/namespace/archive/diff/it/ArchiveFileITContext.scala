/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.it

import java.io.File

import global.namespace.archive.diff.Archive._
import global.namespace.archive.diff.it.ArchiveFileITContext._
import global.namespace.archive.diff.model.DeltaModel
import global.namespace.archive.diff.spi.ArchiveFileStore

/** @author Christian Schlichtherle */
trait ArchiveFileITContext {

  final def deltaModel: DeltaModel = diff.first(test1JarFileStore).second(test2JarFileStore).deltaModel

  final lazy val test1JarFileStore: ArchiveFileStore = jar(file("test1.jar"))

  final lazy val test2JarFileStore: ArchiveFileStore = jar(file("test2.jar"))
}

/** @author Christian Schlichtherle */
private object ArchiveFileITContext {

  def file(resourceName: String) = new File((classOf[ArchiveFileITContext] getResource resourceName).toURI)
}
