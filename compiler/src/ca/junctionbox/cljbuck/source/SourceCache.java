package ca.junctionbox.cljbuck.source;

import ca.junctionbox.cljbuck.lexer.SourceLexer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SourceCache is a cache for raw source code files.
 */
public class SourceCache {
    final ConcurrentHashMap<Path, String> codeCache;

    private SourceCache() {
        codeCache = new ConcurrentHashMap<>();
    }

    public static SourceCache create() {
        final SourceCache cache = new SourceCache();

        return cache;
    }

    public void consume(final Path sourceFile) throws IOException {
        long start = System.currentTimeMillis();
        codeCache.put(sourceFile, new String(Files.readAllBytes(sourceFile), StandardCharsets.UTF_8));
        long finish = System.currentTimeMillis();

        System.out.println("\tconsumed " + sourceFile + " in " + (finish - start) + "ms");
    }

    public void apply(final Path sourceFile, final SourceLexer lexer) {
        lexer.lex(sourceFile, codeCache.get(sourceFile));
    }
}

