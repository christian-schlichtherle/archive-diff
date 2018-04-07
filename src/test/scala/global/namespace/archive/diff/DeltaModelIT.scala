/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff

import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.logging.{Level, Logger}

import global.namespace.archive.diff.Archive.{decode, encode}
import global.namespace.archive.diff.DeltaModelIT._
import global.namespace.archive.diff.it.ArchiveITContext
import global.namespace.archive.diff.model.{DeltaModel, EntryNameAndDigestValue, EntryNameAndTwoDigestValues}
import global.namespace.fun.io.api.Store
import global.namespace.fun.io.bios.BIOS.memoryStore
import org.scalatest.Matchers.{theSameInstanceAs, _}
import org.scalatest.WordSpec
import org.scalatest.prop.PropertyChecks._

import scala.collection.JavaConverters._

/** @author Christian Schlichtherle */
class DeltaModelIT extends WordSpec with ArchiveITContext {

  "A delta model" should {
    "support round-trip encoding and decoding" in {
      forAll(TestCases) { builder =>
        val original = builder.messageDigest(sha1).build
        val store = memoryStore
        encode(store, original)
        val clone = decode(store)
        logger.log(Level.FINE, "\n{0}", utf8String(store))
        clone shouldBe original
        clone should not be theSameInstanceAs(original)
      }
    }
  }
}

private object DeltaModelIT {

  import DeltaModel.{builder => b}
  val TestCases = Table(
    "delta model builder",
    b,
    b.changedEntries(List(new EntryNameAndTwoDigestValues("changed", "1", "2")).asJava),
    b.addedEntries(List(new EntryNameAndDigestValue("added", "1")).asJava),
    b.removedEntries(List(new EntryNameAndDigestValue("removed", "1")).asJava),
    b.unchangedEntries(List(new EntryNameAndDigestValue("unchanged", "1")).asJava)
  )

  val sha1: MessageDigest = MessageDigest getInstance "SHA-1"

  val logger: Logger = Logger.getLogger(getClass.getName)

  def utf8String(store: Store): String = new String(store.content, utf8)

  private val utf8: Charset = Charset forName "UTF-8"
}
