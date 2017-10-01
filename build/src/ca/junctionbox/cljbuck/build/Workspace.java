package ca.junctionbox.cljbuck.build;

import java.io.File;

public class Workspace {
    public final static String WORKSPACE = "WORKSPACE";
    private final String path;

    public Workspace findRoot() {
        File workspace = new File(String.valueOf(new File(WORKSPACE).getAbsoluteFile()));
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

    public String getPath() {
        return path;
    }
}
