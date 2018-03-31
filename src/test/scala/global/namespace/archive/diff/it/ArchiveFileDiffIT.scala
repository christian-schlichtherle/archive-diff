/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.it

import org.scalatest.Matchers._
import org.scalatest.WordSpec

import scala.collection.JavaConverters._

/** @author Christian Schlichtherle */
class ArchiveFileDiffIT extends WordSpec with ArchiveFileITContext {

  "An archive file diff" when {
    "processing the test JAR files" should {
      "partition the entry names and digests correctly" in {
        val model = deltaModel
        import model._
        removedEntries.asScala map (_.entryName) shouldBe List("entryOnlyInFile1")
        addedEntries.asScala map (_.entryName) shouldBe List("entryOnlyInFile2")
        unchangedEntries.asScala map (_.entryName) shouldBe List("META-INF/MANIFEST.MF", "differentEntryTime", "equalEntry")
        changedEntries.asScala map (_.name) shouldBe List("differentEntrySize")
      }
    }
  }
}
