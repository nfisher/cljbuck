# cljbuck
Experiment in speeding up Clojure build times from first principals.

## Design

- No regex in the lexer.
- Pipeline Approach (reader -> lexer -> parser -> evaluator).
- Breadth-First File System Read.
- Eventually fan-out to multiple Lexers/Parsers.
- Reduced "lisp" commands inspired by Buck.

## Potential Performance Improvements

- Pre-Allocate Code Slabs?
- Single reader from Filesystem? (stream files)
- Lower memory pressure/no-GC?

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

### Entries

```
filepath - $path:$pos
symbol slab location - $symbol:$slabloc
```

## Project File Specification

command-alias: 
export-file:
genrule:
remote-file:
worker-tool:
zip-file:

java-binary: Generate executable JAR file.
java-library: Generate library file.
prebuilt-jar: Precompiled binary.

## Points of Inspiration

Aeron Protocol Specification - https://github.com/real-logic/aeron/wiki/Protocol-Specification
Buck Build - https://buckbuild.com/
