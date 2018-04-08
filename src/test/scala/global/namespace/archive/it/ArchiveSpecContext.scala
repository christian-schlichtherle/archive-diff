/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.it

import java.io.File

import global.namespace.archive.api.ArchiveFileSource
import global.namespace.archive.commons.compress.Compress._
import global.namespace.archive.it.ArchiveSpecContext._

/** @author Christian Schlichtherle */
trait ArchiveSpecContext {

  final lazy val Test1Jar: ArchiveFileSource[_] = jar(resourceFile("test1.jar"))

  final lazy val Test2Jar: ArchiveFileSource[_] = jar(resourceFile("test2.jar"))
}

/** @author Christian Schlichtherle */
private object ArchiveSpecContext {

  def resourceFile(resourceName: String): File = new File((classOf[ArchiveSpecContext] getResource resourceName).toURI)
}
