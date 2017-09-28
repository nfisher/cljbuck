package ca.junctionbox.cljbuck.source;

import ca.junctionbox.cljbuck.lexer.SourceLexer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

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
        final long start = System.currentTimeMillis();
        final byte[] bytes = Files.readAllBytes(sourceFile);
        final long read = System.currentTimeMillis();
        final String contents = new String(bytes, StandardCharsets.UTF_8);
        final long finish = System.currentTimeMillis();

        codeCache.put(sourceFile, contents);

        logger.info("" + bytes.length + "B from " + sourceFile + " in " + (finish - start) + "ms, " + (read - start) + "ms read, " + (finish - read) + "ms conversion");
    }

    public void apply(final Path sourceFile, final SourceLexer lexer) {
        lexer.lex(sourceFile, codeCache.get(sourceFile));
    }
}

