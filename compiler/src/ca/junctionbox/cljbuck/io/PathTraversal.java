package ca.junctionbox.cljbuck.io;

import ca.junctionbox.cljbuck.channel.Writer;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;


public class PathTraversal extends SimpleFileVisitor<Path> {
    private final PathMatcher matcher;
    private Writer out;

    private PathTraversal(final String pattern, final Writer out) {
        this.out = out;
        this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
    }

    public static PathTraversal create(final String pattern, final Writer out) {
        return new PathTraversal(pattern, out);
    }

    void find(Path file) {
        Path name = file.getFileName();
        if (name != null && matcher.matches(file)) {
            out.write(file);
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        find(file);
        return CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (dir.getFileName().startsWith(".")) {
            return SKIP_SUBTREE;
        } else if (dir.getFileName().equals("clj-out")) {
            return SKIP_SUBTREE;
        } else if (dir.getFileName().equals("buck-out")) {
            return SKIP_SUBTREE;
        }
        find(dir);
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.err.println(exc);
        return CONTINUE;
    }
}
