# cljbuck [![Build Status](https://travis-ci.org/nfisher/cljbuck.svg?branch=master)](https://travis-ci.org/nfisher/cljbuck)

Experiment in speeding up Clojure build times from the ground up.

## Sample Project Structure

- WORKSPACE - used to mark the root of the project.
- CLJ - sprinkled where desired in a project to specify build targets.

### Simple Project Structure

```
  project
    +-- WORKSPACE
    +-- lib/
    |     +-- CLJ
    |     +-- clojure-1.9.0-beta1.jar
    |     \-- ...
    +-- jbx
        +-- CLJ
        +-- src/
```

#### jbx/CLJ

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

#### lib/CLJ

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

- [X] compile a clojure target by namespace.
- [X] serial compiler.
- [ ] parallel "level" compiler.
- [ ] parallel "event" compiler.
- [X] run a clojure program.
- [X] load clojure repl.
- [X] finish symbol parsing.
- [ ] check for circular dependency violations.
- [ ] noop when there's no source modifications.
- [ ] file IO optimisations (e.g. JNI).
- [ ] remove or root all dependencies required by cljbuck.
- [ ] instrument JMH benchmarks for each stage.
- [ ] AST generation.
- [ ] evaluator.
- [ ] compile individual \*.clj files rather than namespace.
- [ ] generate bytecode.
- [ ] generate jar.
- [ ] generate uberjar.
- [ ] run tests.
- [ ] pom to target generator.
- [X] structured logs for generating perf graphs.
- [X] ms or ns timestamp in structured logs.
- [X] uid for start/finish in structured logs.
- [ ] analyse command/http server to render structured logs.
- [ ] consider instrumenting structured logs to use nanoTime so time values are
  (mostly) guaranteed to be monotonic.
- [ ] allow extension of build rules, should probably turn the build rules into
  a map.

## Building

- requires buck - see https://buckbuild.com/setup/getting_started.html.
- buck test --all # run tests
- buck build //... # build project
- buck run //compiler:main -- "$CLJ_PROJECT" # run

## Compiler Strategies

For all strategies the following process is carried out to evaluate the build
graph:

1. The specified target becomes the root node.
2. The graph is walked breadth first evaluating dependencies.
3. Once all transitive dependencies have been walked to leaf nodes compilation
   can commence.

Serial strategy is the easiest to implement and is likely to cause the fewest
issues with bugs. The other two strategies are expected to provide improved
performance through parallelism.

### Serial

As the tree is walked the dependencies are placed in a stack. Once the build
starts classpaths and compilation occurs from the top of the stack down to the
bottom.

### Level

As the tree is walked the dependencies are placed in an array of lists. The
index for the array is the depth in the tree. The list is simply a collection of
elements that can be processed at the same time because they do not have
peer dependencies. Classpaths and compilation can commence in parallel from the
deepest nodes and will only proceed to the next level once all nodes are
complete in the current level. Using the sample //jbx:main as an example:

```
//jbx:main - main.jar
    +- //jbx:lib - /Users/nathanfisher/workspace/cljbuck/jbx/src/clj/**/*.clj,/Users/nathanfisher/workspace/cljbuck/jbx/src/cljc/**/*.cljc,
    |    +- //lib:clojure1.9 - /Users/nathanfisher/workspace/cljbuck/lib/clojure-1.9.0-beta1.jar
    |    |    +- //lib:core.specs.alpha - /Users/nathanfisher/workspace/cljbuck/lib/core.specs.alpha-0.1.24.jar
    |    |    +- //lib:spec.alpha - /Users/nathanfisher/workspace/cljbuck/lib/spec.alpha-0.1.123.jar
```

The above graph of dependencies would look as follows:

1. (//jbx:lib)
2. (//lib:clojure1.9)
3. (//lib:core.specs.alpha, //lib:spec.alpha)

Similar to the serial strategy processing would start at the furthest point from
the target in this case level 3 would be processed, then level 2, and finally
level 1.

### Notification

Notification will involve an event bus. That all nodes subscribe to as their
dependencies are completed they will enqueue themselves to be processed.

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
- JNI or named pipe interface to C based "glob" service (svc: glob -> filelist).

## Assumptions

Dynamic language makes it more difficult to specify an ABI.

Using multiples of 4096 for number bytes to keep it around memory page
boundaries

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

50 * (3 * 64 + 3904) = 204,800 Bytes

Large

1000 * (3 * 64 + 12096) = 12,288,000 Bytes

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
| SHA256 (8 bytes)                                            |
+-------------------------------------------------------------+
| CLJ Src                               ...  
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

- Aeron Protocol Specification -
  https://github.com/real-logic/aeron/wiki/Protocol-Specification
- Buck Build - https://buckbuild.com/
