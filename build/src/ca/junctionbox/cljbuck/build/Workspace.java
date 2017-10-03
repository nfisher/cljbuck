package ca.junctionbox.cljbuck.build;

import java.io.File;
import java.util.logging.Logger;

public class Workspace {
    public final static String WORKSPACE = "WORKSPACE";
    private final String path;
    private final Logger logger;

    public Workspace findRoot() {
        File workspace = new File(String.valueOf(new File(WORKSPACE).getAbsoluteFile()));
        for (; ;) {
            if (workspace.exists()) {
                break;
            }
            final String parentDir = workspace.getParentFile().getParent();
            workspace = new File(parentDir, WORKSPACE);
        }
        return new Workspace(workspace.getParent(), logger);
    }

    public Workspace(final Logger logger) {
        this("", logger);
    }

    public Workspace(final String path, final Logger logger) {
        this.path = path;
        this.logger = logger;
    }

    public String getPath() {
        return path;
    }
}
