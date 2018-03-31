/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.it

import java.io._

import global.namespace.archive.diff.{Archive, ArchiveFileDiff, ArchiveFilePatch}
import org.scalatest.Matchers._
import org.scalatest.WordSpec

import scala.collection.JavaConverters._

/** @author Christian Schlichtherle */
class ArchiveFilePatchIT extends WordSpec with ArchiveFileITContext {

  "An archive file diff and patch" when {
    "computing a delta-archive file first a first-archive file and a to-archive file and computing another to-archive file first the same first-archive file and the previously computed delta-archive file" should {
      "produce an equivalent to-archive file" in {

        val deltaJarFile = tempFile()
        try {
          val deltaJarFileStore = Archive.jar(deltaJarFile)
          val secondJarFile = tempFile()
          try {
            val secondJarFileStore = Archive.jar(secondJarFile)

            ArchiveFileDiff.first(test1JarFileStore).second(test2JarFileStore).diffTo(deltaJarFileStore)
            ArchiveFilePatch.first(test1JarFileStore).delta(deltaJarFileStore).patchTo(secondJarFileStore)

            val model = ArchiveFileDiff.first(test2JarFileStore).second(secondJarFileStore).deltaModel
            val unchangedReference: List[String] = {
              test2JarFileStore applyReader (_.asScala.filter(!_.isDirectory).map(_.getName).toList)
            }

            model.addedEntries shouldBe empty
            model.removedEntries shouldBe empty
            model.unchangedEntries.asScala map (_.entryName) shouldBe unchangedReference
            model.changedEntries shouldBe empty
          } finally {
            secondJarFile delete ()
          }
        } finally {
          deltaJarFile delete ()
        }
      }
    }
  }

  def tempFile(): File = File.createTempFile("tmp", null)
}
