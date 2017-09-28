package ca.junctionbox.cljbuck.io;

import ca.junctionbox.cljbuck.channel.Writer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;


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
        // need to make this more intelligent so it doesn't scan a sub-tree unnecessarily.
        find(dir);
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.err.println(exc);
        return CONTINUE;
    }
}
