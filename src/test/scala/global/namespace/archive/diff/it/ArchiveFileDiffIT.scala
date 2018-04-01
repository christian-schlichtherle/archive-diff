/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.it

import global.namespace.archive.diff.Archive.diff
import org.scalatest.Matchers._
import org.scalatest.WordSpec

import scala.collection.JavaConverters._

/** @author Christian Schlichtherle */
class ArchiveFileDiffIT extends WordSpec with ArchiveITContext {

  "An archive file diff" when {
    "processing the test JAR files" should {
      "partition the entry names and digests correctly" in {
        val model = (diff first Test1Jar second Test2Jar).deltaModel
        import model._
        changedEntries.asScala map (_.name) shouldBe List("differentEntrySize")
        addedEntries.asScala map (_.entryName) shouldBe List("entryOnlyInFile2")
        removedEntries.asScala map (_.entryName) shouldBe List("entryOnlyInFile1")
        unchangedEntries.asScala map (_.entryName) shouldBe List("META-INF/MANIFEST.MF", "differentEntryTime", "equalEntry")
      }
    }
  }
}
