# Archive I/O [![Maven Central](https://img.shields.io/maven-central/v/global.namespace.archive-io/archive-io-api.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.archive-io%22) [![Build Status](https://api.travis-ci.org/christian-schlichtherle/archive-io.svg)](https://travis-ci.org/christian-schlichtherle/archive-io)

Archive I/O features diffing and patching of archive files like EAR, JAR, WAR, ZIP et al or directories.

## Features

+ An API for transparent access to archive files which is based on the API of [Fun I/O].
+ A facade for accessing JAR and ZIP files which depends on [Apache Commons Compress].
+ Another facade for accessing JAR and ZIP files which depends on the Java Runtime Environment (JRE) only.
+ A facade for diffing and patching archive files or directories.

## Status

This project has been integrated with [Fun I/O] for future development. 
Consequently, Archive I/O 0.10.0 was the last release. 
It's successor is Fun I/O 0.11.0.
All features have been retained in the transition.
However, the API has been slightly changed. 

[Fun I/O]: https://github.com/christian-schlichtherle/fun-io
