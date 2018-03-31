/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.diff.it

import java.io._
import java.security.MessageDigest

import global.namespace.archive.diff.diff.ArchiveFileDiff
import global.namespace.archive.diff.io.{ArchiveFileInput, JarFileStore, MessageDigests}
import global.namespace.archive.diff.patch.ArchiveFilePatch
import org.scalatest.Matchers._
import org.scalatest.WordSpec

import scala.collection.JavaConverters._

/** @author Christian Schlichtherle */
class ArchiveFilePatchIT extends WordSpec with ArchiveFileITContext {

  def tempFile(): File = File.createTempFile("tmp", null)

  def fileEntryNames(input: ArchiveFileInput): List[String] = {
    List.empty[String] ++ input.iterator.asScala.filter(!_.isDirectory).map(_.getName)
  }

  "An archive file diff and patch" when {
    "computing a delta-archive file from a from-archive file and a to-archive file and computing another to-archive file from the same from-archive file and the previously computed delta-archive file" should {
      "produce an equivalent to-archive file" in {

        val deltaJarFile = tempFile()
        try {
          val deltaJarFileStore = new JarFileStore(deltaJarFile)
          val toJarFile = tempFile()
          try {
            val toJarFileStore = new JarFileStore(toJarFile)

            ArchiveFileDiff.builder.from(test1JarFileStore).to(test2JarFileStore).build.outputTo(deltaJarFileStore)
            ArchiveFilePatch.builder.from(test1JarFileStore).delta(deltaJarFileStore).build.outputTo(toJarFileStore)

            test2JarFileStore acceptReader { jar2: ArchiveFileInput =>
              val unchangedReference = fileEntryNames(jar2)

              toJarFileStore acceptReader { updated: ArchiveFileInput =>
                val model = new ArchiveFileDiff.Engine {

                  val digest: MessageDigest = MessageDigests.sha1

                  def from: ArchiveFileInput = jar2

                  def to: ArchiveFileInput = updated
                } model ()
                model.addedEntries shouldBe empty
                model.removedEntries shouldBe empty
                model.unchangedEntries.asScala map (_.name) shouldBe unchangedReference
                model.changedEntries shouldBe empty
                ()
              }
            }
          } finally {
            toJarFile delete ()
          }
        } finally {
          deltaJarFile delete ()
        }
      }
    }
  }
}
