/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff

import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.logging.{Level, Logger}

import global.namespace.archive.diff.Archive.{decodeFromXml, encodeToXml}
import global.namespace.archive.diff.DeltaModelIT._
import global.namespace.archive.diff.it.ArchiveITContext
import global.namespace.archive.diff.model.DeltaModel
import global.namespace.fun.io.api.Store
import global.namespace.fun.io.bios.BIOS.memoryStore
import org.scalatest.Matchers.{theSameInstanceAs, _}
import org.scalatest.WordSpec

/** @author Christian Schlichtherle */
class DeltaModelIT extends WordSpec with ArchiveITContext {

  "A delta model" when {
    "constructed with no data" should {
      "be round-trip XML-serializable" in {
        assertRoundTripXmlSerializable(DeltaModel.builder.messageDigest(MessageDigest.getInstance("SHA-1")).build)
      }
    }

    "computed from an archive file diff" should {
      "be round-trip XML-serializable" in {
        assertRoundTripXmlSerializable(deltaModel)
      }
    }
  }
}

private object DeltaModelIT {

  final def assertRoundTripXmlSerializable(original: DeltaModel) {
    val store = memoryStore
    encodeToXml(original, store)
    val clone = decodeFromXml(store)
    logger.log(Level.INFO, "\n{0}", utf8String(store))
    clone shouldBe original
    clone should not be theSameInstanceAs(original)
  }

  val logger: Logger = Logger.getLogger(getClass.getName)

  def utf8String(store: Store): String = new String(store.content, utf8)

  private val utf8: Charset = Charset forName "UTF-8"
}