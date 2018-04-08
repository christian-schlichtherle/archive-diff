/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.api

import java.util.zip.ZipEntry

import global.namespace.fun.io.bios.BIOS.copy
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.WordSpec
import org.scalatest.mockito.MockitoSugar.mock

/** @author Christian Schlichtherle */
class ArchiveEntryChannelSpec extends WordSpec {

  "An archive file entry" should {
    "support direct data transer to another archive file entry" in {
      val source = mock[ArchiveEntrySource[ZipEntry]]
      when(source connect any[ArchiveEntrySink[_]]) thenCallRealMethod ()

      val sink = mock[ArchiveEntrySink[ZipEntry]]
      when(sink connect any[ArchiveEntrySource[_]]) thenCallRealMethod ()

      val channel1 = source connect sink
      val channel2 = sink connect source

      pending

      copy(channel1.source, channel1.sink)
      copy(channel2.source, channel2.sink)
    }
  }
}
