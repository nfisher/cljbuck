# cljbuck

[![Build Status](https://travis-ci.org/nfisher/cljbuck.svg?branch=master)](https://travis-ci.org/nfisher/cljbuck)

Experiment in speeding up Clojure build times from the ground up.

## Sample Project Structure

- WORKSPACE - used to mark the root of the project.
- BUILD - sprinkled where desired in a project to specify build targets.

### Simple Project Structure

```
project
 +-- WORKSPACE
 +-- lib/
 |     +-- BUILD
 |     +-- clojure-1.9.0-beta1.jar
 |     \-- ...
 +-- jbx
       +-- BUILD
       +-- src/
```

#### lib/BUILD

```
(jar :name "clojure1.9"
     :jar "clojure-1.9.0-beta1.jar"
     :deps [":core.specs.alpha", ":spec.alpha"]
     :visibility ["PUBLIC"])

(jar :name "spec.alpha"
     :jar "spec.alpha-0.1.123.jar")

(jar :name "core.specs.alpha"
     :jar "core.specs.alpha-0.1.24.jar")
```

#### jbx/BUILD

```
(clj-lib :name "lib"
         :ns "jbx.core"
         :srcs ["src/clj/**/*.clj", "src/cljc/**/*.cljc"]
         :deps ["//lib:clojure1.9"])

(clj-binary :name "main"
            :main "jbx.core"
            :deps [":lib"])

(clj-test :name "test"
          :srcs ["test/clj/**/*.clj"]
          :deps ["//lib:clojure1.9", ":lib"])

```

## Next Steps

- instrument JMH benchmarks for each stage.
- finish symbol parsing.
- build AST.
- build evaluator.
- generate bytecode, jar file, etc.
- run a clojure program.

## Building

- requires buck - see https://buckbuild.com/setup/getting_started.html.
- buck test --all # run tests
- buck build //... # build project
- buck run //compiler:main -- "$CLJ_PROJECT" # run

## Design

- No regex in the lexer.
- Pipeline approach (reader -> lexer -> parser -> evaluator).
- Reduced "lisp" commands inspired by Buck.
- Build a DAG for all forms.

## Potential Performance Improvements

- Breadth-First File System Read?
- Pre-Allocate Code Slabs?
- Single reader from Filesystem? (stream files)
- Lower memory pressure/no-GC?
- Fan-out to multiple Lexers/Parsers/Evaluators?

## Assumptions

Dynamic language makes it more difficult to specify an ABI.

Using multiples of 4096 for number bytes to keep it around memory page boundaries

  File Size

  - Average <4,096 Bytes.
  - 90PCTL <12,288 Bytes.

  Project Size

  - Average 50 files.
  - 95PCTL < 1000 files.

  Dependency Size

  - Average 75 deps.
  - 95PCTL < 500 deps.

## Memory Estimates

```
Small

50 * (3 * 64 + 3904)
= 204,800 Bytes

Large

1000 * (3 * 64 + 12096)
= 12,288,000 Bytes

Dependencies

??
```

## Slab Structure

- Data stored in raw bytes.
- Src length is byte len of file.
- Frame length is total size of frame less header.
- SHA256 is the checksum of the file.

```
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |  Frame Length (int)       |  Src Length (int)               |
    +-------------------------------------------------------------+
    |                       SHA256 (8 bytes)                      |
    +-------------------------------------------------------------+
    |                       CLJ Src                               ...
```

## Project File Specification

- command-alias: 
- export-file:
- genrule:
- remote-file:
- worker-tool:
- zip-file:

- java-binary: Generate executable JAR file.
- java-library: Generate library file.
- prebuilt-jar: Precompiled binary.

## Inspiration

- Aeron Protocol Specification - https://github.com/real-logic/aeron/wiki/Protocol-Specification
- Buck Build - https://buckbuild.com/
