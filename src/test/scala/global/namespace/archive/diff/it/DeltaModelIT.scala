/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.it

import global.namespace.archive.diff.io.MessageDigests.sha1
import global.namespace.archive.diff.model.DeltaModel
import org.scalatest.WordSpec

/** @author Christian Schlichtherle */
class DeltaModelIT extends WordSpec with ArchiveFileITContext {

  "A delta model" when {
    "constructed with no data" should {
      "be round-trip XML-serializable" in {
        assertRoundTripXmlSerializable(DeltaModel.builder.messageDigest(sha1).build)
      }
    }

    "computed from an archive file diff" should {
      "be round-trip XML-serializable" in {
        assertRoundTripXmlSerializable(loanArchiveFileDiffEngine(_ model ()))
      }
    }
  }
}
