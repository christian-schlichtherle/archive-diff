# Archive I/O [![Maven Central](https://img.shields.io/maven-central/v/global.namespace.archive-io/archive-io.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.archive-io%22) [![Build Status](https://api.travis-ci.org/christian-schlichtherle/archive-io.svg)](https://travis-ci.org/christian-schlichtherle/archive-io)

This library features diffing and patching of archive files like EAR, JAR, WAR, ZIP et al.

## Features

Archive I/O features:

+ An API for transparent access to archive files which is based on the API of [Fun I/O].
+ A facade for accessing JAR and ZIP files which depends on [Apache Commons Compress].
+ Another facade for accessing JAR and ZIP files which depends on `java.util.jar` and `java.util.zip`.
+ A facade for diffing and patching archive files.

## Structure

Archive I/O has a modular structure and its artifacts are hosted on Maven Central with the common group ID
`global.namespace.archive-io`.
The following diagram shows the module structure:

@startuml

folder global.namespace.archive-io {
    [archive-io-api] <|-- [archive-io-commons-compress]
    [archive-io-api] <-- [archive-io-delta]
    [archive-io-api] <|-- [archive-io-juz]
    [archive-io-juz] -[hidden] [archive-io-commons-compress]
}

[commons-compress] <--- [archive-io-commons-compress]
[fun-io-jackson] <--- [archive-io-delta]
[fun-io-api] <-- [archive-io-api]
[fun-io-api] <-- [fun-io-bios]
[fun-io-api] <-- [fun-io-jackson]
[fun-io-bios] <--- [archive-io-commons-compress]
[fun-io-bios] <--- [archive-io-delta]
[fun-io-bios] <--- [archive-io-juz]

[archive-io-delta] "this" <-- [client-application]
[archive-io-juz] "and either this" <.. [client-application]
[archive-io-commons-compress] "or that" <.. [client-application]

@enduml

The modules are:

+ `archive-io-api`: Provides the API for accessing archive files.
  The base package of this module is `global.namespace.archive.io.api`.
+ `archive-io-commons-compress`: Implements the API and provides a facade for accessing JAR and ZIP files.
  This module depends on Apache Commons Compress and provides best performance for diffing and patching.
  The base package of this module is `global.namespace.archive.io.commons.compress`.
+ `archive-io-juz`: Implements the API and provides a facade for accessing JAR and ZIP files.
  This module depends on the packages `java.util.jar` and `java.util.zip` (not shown).
  The base package of this module is `global.namespace.archive.io.juz`.
+ `archive-io-delta`: Provides a facade for diffing and patching archive files.
  The base package of this module is `global.namespace.archive.io.delta`.

Thus, for diffing and patching, your application needs to depend on the modules `archive-io-delta` and either
`archive-io-commons-compress` or `archive-io-juz`.

## Usage

### Diffing two JAR files and generating a delta JAR file

The following code diffs two JAR files and generates a delta JAR file.
It uses the `Compress` facade to access the JAR files using Apache Commons Compress.
It also uses the `Delta` facade for the actual diffing.

```java
import java.io.File;

import static global.namespace.archive.io.commons.compress.Compress.*;
import static global.namespace.archive.io.delta.Delta.*;

File first = ...;
File second = ...;
File delta = ...;
diff().first(jar(first)).second(jar(second)).to(jar(delta));
```

If you wanted to use the `archive-io-juz` module instead of the `archive-io-commons-compress` module, then, apart from
configuring the class path, you would only have to edit the `import` statement as shown in the next example.

### Patching a JAR file from a delta JAR file to another JAR file

The following code patches a JAR file from a delta JAR file to another JAR file.
It uses the `JUZ` facade to access the JAR files using `java.util.jar`.
It also uses the `Delta` facade for the actual patching.

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

Maybe you just want to explore the delta of two archive files, but not generate another archive file from that?
The following code diffs two ZIP files and computes a delta model.
Again, the `Delta` and either the `JUZ` or `Compress` facades can be used to do that:

```java
import java.io.File;

import global.namespace.archive.io.delta.model.*;

import static global.namespace.archive.io.delta.Delta.*;
import static global.namespace.archive.io.juz.JUZ.*;

File first = ...;
File second = ...;
DeltaModel model = diff().first(zip(first)).second(zip(second)).deltaModel();
```

The delta model has properties describing the changed, unchanged, added and removed entries.

[Apache Commons Compress]: https://commons.apache.org/proper/commons-compress/
[Fun I/O]: https://github.com/christian-schlichtherle/fun-io
