package ca.junctionbox.cljbuck.lexer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static ca.junctionbox.cljbuck.build.json.Event.finished;
import static ca.junctionbox.cljbuck.build.json.Event.started;

/**
 * SourceCache is a cache for raw source code files.
 */
public class SourceCache {
    final ConcurrentHashMap<Path, String> codeCache;
    private final Logger logger;

    private SourceCache(final Logger logger) {
        this.codeCache = new ConcurrentHashMap<>();
        this.logger = logger;
    }

    public static SourceCache create(final Logger logger) {
        final SourceCache cache = new SourceCache(logger);

        return cache;
    }

    public void consume(final Path sourceFile) throws IOException {
        logger.info(started(hashCode())
                        .add("source", sourceFile)
                        .toString());

        final long start = System.currentTimeMillis();
        final byte[] bytes = Files.readAllBytes(sourceFile);
        final long read = System.currentTimeMillis();
        final String contents = new String(bytes, StandardCharsets.UTF_8);

        codeCache.put(sourceFile, contents);

        logger.info(finished(hashCode(), start)
                .add("source", sourceFile)
                .add("bytes", bytes.length)
                .add("read", (read - start))
                .toString());
    }

    public void apply(final Path sourceFile, final SourceLexer lexer) {
        lexer.lex(sourceFile, codeCache.get(sourceFile));
    }
}

