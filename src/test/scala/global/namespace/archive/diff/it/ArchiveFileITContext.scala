/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.it

import java.io.File
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.logging.{Level, Logger}

import global.namespace.archive.diff.diff.ArchiveFileDiff
import global.namespace.archive.diff.io.{ArchiveFileInput, JarFileStore, MessageDigests}
import global.namespace.archive.diff.it.ArchiveFileITContext._
import global.namespace.archive.diff.model.DeltaModel.jaxbContext
import global.namespace.fun.io.api.{Codec, Store}
import global.namespace.fun.io.bios.BIOS.memoryStore
import global.namespace.fun.io.jaxb.JAXB
import javax.xml.bind.{Marshaller, Unmarshaller}
import org.scalatest.Matchers.{equal, theSameInstanceAs, _}

/** @author Christian Schlichtherle */
trait ArchiveFileITContext {

  final def loanArchiveFileDiffEngine[A](fun: ArchiveFileDiff.Engine => A): A = {
    test1JarFileStore.applyReader[A] { jar1: ArchiveFileInput =>
      test2JarFileStore.applyReader[A] { jar2: ArchiveFileInput =>
        fun(new ArchiveFileDiff.Engine {

          lazy val digest: MessageDigest = MessageDigests.sha1

          def firstInput: ArchiveFileInput = jar1

          def secondInput: ArchiveFileInput = jar2
        })
      }
    }
  }

  final lazy val test1JarFileStore: JarFileStore = new JarFileStore(file("test1.jar"))

  final lazy val test2JarFileStore: JarFileStore = new JarFileStore(file("test2.jar"))

  final def assertRoundTripXmlSerializable(original: AnyRef) {
    val store = memoryStore
    val clone = jaxbCodec connect store clone original
    logger.log(Level.FINE, "\n{0}", utf8String(store))
    clone should equal (original)
    clone should not be theSameInstanceAs (original)
  }
}

/** @author Christian Schlichtherle */
private object ArchiveFileITContext {

  def file(resourceName: String) = new File((classOf[ArchiveFileITContext] getResource resourceName).toURI)

  val jaxbCodec: Codec = JAXB.xmlCodec(jaxbContext, modifyMarshaller _, unmarshallerModifier _)

  private def modifyMarshaller(m: Marshaller): Unit = m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)

  private def unmarshallerModifier(u: Unmarshaller): Unit = { }

  val logger: Logger = Logger.getLogger(getClass.getName)

  def utf8String(store: Store): String = new String(store.content, utf8)

  private val utf8: Charset = Charset forName "UTF-8"
}
