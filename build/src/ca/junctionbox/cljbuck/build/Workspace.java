package ca.junctionbox.cljbuck.build;

import java.io.File;

public class Workspace {
    public final static String WORKSPACE = "WORKSPACE";
    public static final String CLJ = "/CLJ";
    private final String path;

    public Workspace findRoot() {
        File workspace = new File(WORKSPACE).getAbsoluteFile();

        for (; ;) {
            if (workspace.exists()) {
                break;
            }
            final String parentDir = workspace.getParentFile().getParent();
            workspace = new File(parentDir, WORKSPACE);
        }

        return new Workspace(workspace.getParent());
    }

    public Workspace() {
        this("");
    }

    public Workspace(final String path) {
        this.path = path;
    }

    public String workspaceRelative(final String targetSource, final String name) {
        if (targetSource.startsWith(path)) {
            final String wsFilename = targetSource.substring(path.length());
            if (CLJ.length() == wsFilename.length()) {
                // strips CLJ for root
                return "/" + wsFilename.substring(0, wsFilename.length()-3) + ":" + name;
            }
            // strips /CLJ
            return "/" + wsFilename.substring(0, wsFilename.length()-CLJ.length()) + ":" + name;
        }
        return "";
    }

    public String getPath() {
        return path;
    }

    public String getOutputDir() {
        return path + "/clj-out";
    }

    public String workspaceAbsolute(final String targetSource, final String targetRelative) {
        if (targetSource.startsWith(path)) {
            final String basedir = targetSource.substring(0, targetSource.length() - 3);
            return basedir + targetRelative;
        }
        return "";
    }
}
