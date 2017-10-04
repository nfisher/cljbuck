package ca.junctionbox.cljbuck.build;

import java.io.File;
import java.util.logging.Logger;

public class Workspace {
    public final static String WORKSPACE = "WORKSPACE";
    public static final String CLJ = "/CLJ";
    private final String path;
    private final Logger logger;


    public Workspace findRoot() {
        final long started = System.currentTimeMillis();
        logger.info("\"event\":\"started\"");

        File workspace = new File(WORKSPACE).getAbsoluteFile();

        for (; ;) {
            if (workspace.exists()) {
                break;
            }
            System.out.println("searching...");
            final String parentDir = workspace.getParentFile().getParent();
            workspace = new File(parentDir, WORKSPACE);
        }

        final long finished = System.currentTimeMillis();
        logger.info("\"event\":\"finished\",\"total\":" + (finished-started));
        return new Workspace(logger, workspace.getParent());
    }

    public Workspace(final Logger logger) {
        this(logger, "");
    }

    public Workspace(final Logger logger, final String path) {
        this.path = path;
        this.logger = logger;
    }

    public String workspaceRelative(final String absolute, final String name) {
        if (absolute.startsWith(path)) {
            final String wsFilename = absolute.substring(path.length());
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
}
