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
        final long start = System.currentTimeMillis();
        final byte[] bytes = Files.readAllBytes(sourceFile);
        final long read = System.currentTimeMillis();
        final String contents = new String(bytes, StandardCharsets.UTF_8);
        final long finish = System.currentTimeMillis();
        codeCache.put(sourceFile, contents);

        System.out.println("\tconsumed " + bytes.length + "B from " + sourceFile + " in " + (finish - start) + "ms, " + (read - start) + "ms read, " + (finish - read) + "ms conversion");
    }

    public void apply(final Path sourceFile, final SourceLexer lexer) {
        lexer.lex(sourceFile, codeCache.get(sourceFile));
    }
}

