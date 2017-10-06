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
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;


public class PathTraversal extends SimpleFileVisitor<Path> {
    private final PathMatcher matcher;
    private Writer out;
    private final String workspacePath;

    private PathTraversal(final String pattern, final Writer out, String workspacePath) {
        this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        this.out = out;
        this.workspacePath = workspacePath;
    }

    public static PathTraversal create(final String pattern, final Writer out, final String workspacePath) {
        return new PathTraversal(pattern, out, workspacePath);
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
        final String d = dir.getFileName().toString();
        final String abs = dir.toAbsolutePath().toString();

        if (d.startsWith(".")) {
            return SKIP_SUBTREE;
        } else if (abs.equals(workspacePath+"/clj-out")) {
            return SKIP_SUBTREE;
        } else if (abs.equals(workspacePath+"/target")) {
            return SKIP_SUBTREE;
        } else if (abs.equals(workspacePath+"/buck-out")) {
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
