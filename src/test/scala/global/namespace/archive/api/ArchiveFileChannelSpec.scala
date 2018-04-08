/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.api

import java.util.zip.ZipEntry

import global.namespace.fun.io.bios.BIOS.copy
import org.scalatest.WordSpec
import org.scalatest.mockito.MockitoSugar.mock
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._

/** @author Christian Schlichtherle */
class ArchiveFileChannelSpec extends WordSpec {

  "An archive file entry" should {
    "support direct data transer to another archive file entry" in {
      val input = mock[ArchiveFileInput[ZipEntry]]
      when(input source any[ArchiveFileEntry[ZipEntry]]) thenCallRealMethod ()

      val output = mock[ArchiveFileOutput[ZipEntry]]
      when(output sink any[ArchiveFileEntry[ZipEntry]]) thenCallRealMethod ()

      val inputEntry = mock[ArchiveFileEntry[ZipEntry]]
      val outputEntry = mock[ArchiveFileEntry[ZipEntry]]

      val channel = input.source(inputEntry).connect(output.sink(outputEntry))

      pending

      copy(channel.source, channel.sink)
    }
  }
}
