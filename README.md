# Archive Diff [![Maven Central](https://img.shields.io/maven-central/v/global.namespace.archive-diff/archive-diff.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.archive-diff%22) [![Build Status](https://api.travis-ci.org/christian-schlichtherle/archive-diff.svg)](https://travis-ci.org/christian-schlichtherle/archive-diff)

Diffs and patches archive files like EAR, JAR, WAR, ZIP et al.

## Usage

### Diffing two JAR files and generating a delta JAR file

```java
import java.io.File;

import static global.namespace.archive.io.delta.Delta.*;

File first = ...;
File second = ...;
File delta = ...;
diff().first(jar(first)).second(jar(second)).to(jar(delta));
```

### Patching a JAR file from a generated delta JAR file

```java
import java.io.File;

import static global.namespace.archive.io.delta.Delta.*;

File first = ...;
File second = ...;
File delta = ...;
patch().first(jar(first)).delta(jar(delta)).to(jar(second));
```

### Diffing two ZIP files and computing a delta model

Maybe you just want to explore the differences, but not generate a delta archive file:

```java
import java.io.File;

import global.namespace.archive.io.delta.model.*;

import static global.namespace.archive.io.delta.Delta.*;

File first = ...;
File second = ...;
DeltaModel model = diff().first(zip(first)).second(zip(second)).deltaModel();
```

[Apache Commons Compress]: https://commons.apache.org/proper/commons-compress/
[JAXB]: https://javaee.github.io/jaxb-v2/
[XZ for Java]: https://tukaani.org/xz/
