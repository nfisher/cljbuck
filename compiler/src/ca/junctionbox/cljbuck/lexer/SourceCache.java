package ca.junctionbox.cljbuck.lexer;

import ca.junctionbox.cljbuck.build.json.Tracer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

import static ca.junctionbox.cljbuck.build.json.Event.finished;
import static ca.junctionbox.cljbuck.build.json.Event.started;
import static ca.junctionbox.cljbuck.build.json.JsonKeyPair.jsonPair;

/**
 * SourceCache is a cache for raw source code files.
 */
public class SourceCache {
    final ConcurrentHashMap<Path, String> codeCache;
    private final Tracer tracer;

    private SourceCache(final Tracer tracer) {
        this.codeCache = new ConcurrentHashMap<>();
        this.tracer = tracer;
    }

    public static SourceCache create(final Tracer tracer) {
        final SourceCache cache = new SourceCache(tracer);

        return cache;
    }

    public void consume(final Path sourceFile) throws IOException {
        tracer.info(started(hashCode())
                .addRaw("args", jsonPair()
                        .add("source", sourceFile)
                        .toMapString())
                .toString());

        final long start = System.currentTimeMillis();
        final byte[] bytes = Files.readAllBytes(sourceFile);
        final long read = System.currentTimeMillis();
        final String contents = new String(bytes, StandardCharsets.UTF_8);

        codeCache.put(sourceFile, contents);

        tracer.info(finished(hashCode())
                .addRaw("args", jsonPair()
                        .add("source", sourceFile)
                        .add("bytes", bytes.length)
                        .add("read", (read - start))
                        .toMapString())
                .toString());
    }

    public void apply(final Path sourceFile, final SourceLexer lexer) {
        lexer.lex(sourceFile, codeCache.get(sourceFile));
    }
}

