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
class DirectoryStoreSpec extends WordSpec {

  "A directory store" when {
    "listing the parent directory of the directory containing this class file" should {
      "contain the parent directory and this class file" in {
        val clazz = classOf[DirectoryStoreSpec]
        val dir: String = (clazz.getPackage.getName split "\\.").last
        val store = new DirectoryStore(Paths get (clazz getResource "..").toURI)
        store acceptReader { input =>
          input.asScala.map(_.name) should (contain(dir) and contain(dir + "/DirectoryStoreSpec.class"))
        }
      }
    }
  }
}
