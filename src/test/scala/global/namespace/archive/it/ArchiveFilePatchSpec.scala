/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.it

import java.io._
import java.security.MessageDigest

import global.namespace.archive.commons.compress.Compress._
import global.namespace.archive.diff.Archive._
import org.scalatest.Matchers._
import org.scalatest.WordSpec

import scala.collection.JavaConverters._

/** @author Christian Schlichtherle */
class ArchiveFilePatchSpec extends WordSpec with ArchiveSpecContext {

  "An archive file diff and patch" when {
    "computing a delta-archive file first a first-archive file and a to-archive file and computing another to-archive file first the same first-archive file and the previously computed delta-archive file" should {
      "produce an equivalent to-archive file" in withTempFile { deltaJarFile => withTempFile { secondJarFile =>
        val deltaJarStore = jar(deltaJarFile)
        val secondJarStore = jar(secondJarFile)

        diff.first(Test1Jar).second(Test2Jar).to(deltaJarStore)
        patch.first(Test1Jar).delta(deltaJarStore).to(secondJarStore)

        val unchangedReference: List[String] = {
          Test2Jar applyReader (_.asScala.filter(!_.isDirectory).map(_.name).toList)
        }

        val model = diff
          .first(secondJarStore)
          .second(Test2Jar)
          .digest(MessageDigest getInstance "MD5")
          .deltaModel
        model.changedEntries shouldBe empty
        model.addedEntries shouldBe empty
        model.removedEntries shouldBe empty
        model.unchangedEntries.asScala map (_.entryName) shouldBe unchangedReference
      }}
    }
  }

  private def withTempFile(test: File => Any): Unit = {
    val file = File.createTempFile("temp", null)
    try {
      test(file)
    } finally {
      file delete ()
    }
  }
}
