/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.it

import org.scalatest.Matchers._
import org.scalatest.WordSpec

/** @author Christian Schlichtherle */
class ArchiveFileDiffIT extends WordSpec with ArchiveFileITContext {

  "An archive file diff" when {
    "processing the test JAR files" should {
      "partition the entry names and digests correctly" in {
        val model = loanArchiveFileDiffEngine(_ model ())
        import model._

        import collection.JavaConverters._
        removedEntries.asScala map (_.name) shouldBe List("entryOnlyInFile1")
        addedEntries.asScala map (_.name) shouldBe List("entryOnlyInFile2")
        unchangedEntries.asScala map (_.name) shouldBe List("META-INF/MANIFEST.MF", "differentEntryTime", "equalEntry")
        changedEntries.asScala map (_.name) shouldBe List("differentEntrySize")
      }
    }
  }
}
