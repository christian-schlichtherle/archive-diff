/*
 * Copyright © 2017-2018 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import BuildSettings._
import Dependencies._

lazy val root: Project = project
  .in(file("."))
  .settings(releaseSettings)
  .settings(aggregateSettings)
  .aggregate(api, commonsCompress, delta, it, juz)
  .settings(name := "Archive I/O")

lazy val api: Project = project
  .in(file("api"))
  .settings(javaLibrarySettings)
  .settings(
    libraryDependencies ++= Seq(
      FunIoApi
    ),
    name := "Archive I/O API",
    normalizedName := "archive-io-api"
  )

lazy val commonsCompress: Project = project
  .in(file("commons-compress"))
  .settings(javaLibrarySettings)
  .dependsOn(api)
  .settings(
    libraryDependencies ++= Seq(
      CommonsCompress,
      FunIoBios
    ),
    name := "Archive I/O Commons Compress",
    normalizedName := "archive-io-commons-compress"
  )

lazy val delta: Project = project
  .in(file("delta"))
  .settings(javaLibrarySettings)
  .dependsOn(api)
  .settings(
    libraryDependencies ++= Seq(
      FunIoBios,
      FunIoJackson,
      Scalacheck % Test,
      Scalatest % Test
    ),
    name := "Archive I/O Delta",
    normalizedName := "archive-io-delta"
  )

lazy val it: Project = project
  .in(file("it"))
  .settings(javaLibrarySettings)
  .dependsOn(commonsCompress, delta, juz)
  .settings(
    libraryDependencies ++= Seq(
      Scalacheck % Test,
      Scalatest % Test
    ),
    name := "Archive I/O IT",
    publishArtifact := false
  )

lazy val juz: Project = project
  .in(file("juz"))
  .settings(javaLibrarySettings)
  .dependsOn(api)
  .settings(
    libraryDependencies ++= Seq(
      FunIoBios,
      Scalatest % Test
    ),
    name := "Archive I/O JUZ",
    normalizedName := "archive-io-juz"
  )
