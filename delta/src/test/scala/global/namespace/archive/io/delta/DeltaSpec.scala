/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.io.delta

import java.nio.charset.Charset
import java.security.MessageDigest

import global.namespace.archive.io.delta.Delta._
import global.namespace.archive.io.delta.DeltaSpec._
import global.namespace.archive.io.delta.model.{DeltaModel, EntryNameAndDigestValue, EntryNameAndTwoDigestValues}
import global.namespace.fun.io.api.Store
import global.namespace.fun.io.bios.BIOS.memoryStore
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.prop.PropertyChecks._

import scala.collection.JavaConverters._

/** @author Christian Schlichtherle */
class DeltaSpec extends WordSpec {

  "A delta model" should {
    "support round-trip encoding/decoding to/from JSON" in {
      forAll(TestCases) { (builder, json) =>
        val original = (builder messageDigest sha1).build
        val store = memoryStore
        encodeModel(store, original)
        utf8String(store) shouldBe json
        val clone = decodeModel(store)
        clone shouldBe original
        clone should not be theSameInstanceAs(original)
      }
    }
  }
}

private object DeltaSpec {

  import DeltaModel.{builder => b}
  val TestCases = Table(
    ("delta model builder", "JSON string"),
    (b,
      """{"algorithm":"SHA-1"}"""),
    (b changedEntries List(new EntryNameAndTwoDigestValues("changed", "1", "2")).asJava,
      """{"algorithm":"SHA-1","changed":[{"name":"changed","first":"1","second":"2"}]}"""),
    (b addedEntries List(new EntryNameAndDigestValue("added", "1")).asJava,
      """{"algorithm":"SHA-1","added":[{"name":"added","digest":"1"}]}"""),
    (b removedEntries List(new EntryNameAndDigestValue("removed", "1")).asJava,
      """{"algorithm":"SHA-1","removed":[{"name":"removed","digest":"1"}]}"""),
    (b unchangedEntries List(new EntryNameAndDigestValue("unchanged", "1")).asJava,
      """{"algorithm":"SHA-1","unchanged":[{"name":"unchanged","digest":"1"}]}""")
  )

  val sha1: MessageDigest = MessageDigest getInstance "SHA-1"

  def utf8String(store: Store): String = new String(store.content, utf8)

  private val utf8: Charset = Charset forName "UTF-8"
}
