/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.it

import java.io._
import java.security.MessageDigest

import global.namespace.archive.diff.Archive._
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
          val deltaJarFileStore = jar(deltaJarFile)
          val secondJarFile = tempFile()
          try {
            val secondJarFileStore = jar(secondJarFile)

            diff
              .first(test1JarFileStore)
              .second(test2JarFileStore)
              .to(deltaJarFileStore)
            patch
              .first(test1JarFileStore)
              .delta(deltaJarFileStore)
              .to(secondJarFileStore)


            val unchangedReference: List[String] = {
              test2JarFileStore applyReader (_.asScala.filter(!_.isDirectory).map(_.getName).toList)
            }

            val model = diff
              .first(test2JarFileStore)
              .second(secondJarFileStore)
              .digest(MessageDigest.getInstance("MD5"))
              .deltaModel
            model.changedEntries shouldBe empty
            model.addedEntries shouldBe empty
            model.removedEntries shouldBe empty
            model.unchangedEntries.asScala map (_.entryName) shouldBe unchangedReference
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
