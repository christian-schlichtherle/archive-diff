/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package global.namespace.archive.io.dir

import java.nio.file.Paths

import org.scalatest.Matchers._
import org.scalatest.WordSpec
import scala.collection.JavaConverters._

/** @author Christian Schlichtherle */
class DirSpec extends WordSpec {

  "Dir.dir" when {
    "returning an archive file store for the parent directory of the class file for the current class" should {
      val store = Dir dir (Paths get (classOf[DirSpec] getResource "..").toURI)

      "list all entries in this directory" in {
        store acceptReader { input =>
          input.asScala.map(_.name) shouldBe List("dir", "dir/DirSpec.class")
        }
      }
    }
  }
}
