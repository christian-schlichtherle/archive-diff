# Archive I/O [![Maven Central](https://img.shields.io/maven-central/v/global.namespace.archive-diff/archive-diff.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.archive-diff%22) [![Build Status](https://api.travis-ci.org/christian-schlichtherle/archive-diff.svg)](https://travis-ci.org/christian-schlichtherle/archive-diff)

This library features diffing and patching of archive files like EAR, JAR, WAR, ZIP et al.

## Features

Archive I/O provides:

+ An API for transparent access to archive files which is based on the API of [Fun I/O].
+ A facade for accessing JAR and ZIP files using [Apache Commons Compress].
+ A facade for accessing JAR and ZIP files using `java.util.jar` and `java.util.zip`.
+ A facade for diffing two archive files and generating a delta archive file.
+ A facade for patching an archive file from a generated delta archive file to another archive file.

## Usage

### Diffing two JAR files and generating a delta JAR file

The following code diffs to JAR files and generates a delta JAR file.
It uses the facade of the `Compress` class to access the JAR files using Apache Commons Compress.
It also uses the facade of the `Delta` class for the actual diffing.

```java
import java.io.File;

import static global.namespace.archive.io.commons.compress.Compress.*;
import static global.namespace.archive.io.delta.Delta.*;

File first = ...;
File second = ...;
File delta = ...;
diff().first(jar(first)).second(jar(second)).to(jar(delta));
```

### Patching a JAR file from a generated delta JAR file to another JAR file

The following code patches a JAR file from a generated JAR file to another JAR file.
It uses the facade of the `JUZ` class to access the JAR files using `java.util.jar`.
It also uses the facade of the `Delta` class for the actual patching.

```java
import java.io.File;

import static global.namespace.archive.io.delta.Delta.*;
import static global.namespace.archive.io.juz.JUZ.*;

File first = ...;
File second = ...;
File delta = ...;
patch().first(jar(first)).delta(jar(delta)).to(jar(second));
```

### Diffing two ZIP files and computing a delta model

Maybe you just want to explore the differences, but not generate a delta archive file?
Again, the facades of the `JUZ` (or `Compress`) and `Delta` classes can be used to do that:

```java
import java.io.File;

import global.namespace.archive.io.delta.model.*;

import static global.namespace.archive.io.delta.Delta.*;
import static global.namespace.archive.io.juz.JUZ.*;

File first = ...;
File second = ...;
DeltaModel model = diff().first(zip(first)).second(zip(second)).deltaModel();
```

[Apache Commons Compress]: https://commons.apache.org/proper/commons-compress/
[Fun I/O]: https://github.com/christian-schlichtherle/fun-io
