package ca.junctionbox.cljbuck.build;

import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class WorkspaceTest {
    static final Logger logger = Logger.getLogger("pants");
    static {
        logger.setLevel(Level.OFF);
    }

    @Test
    public void Test_workspaceRelative_subdir() {
        Workspace workspace = new Workspace("/home/nfisher/workspace/pedestal");
        assertThat(workspace.workspaceRelative("/home/nfisher/workspace/pedestal/lib/CLJ", "lib"), is("//lib:lib"));
    }

    @Test
    public void Test_workspaceRelative_root() {
        Workspace workspace = new Workspace("/home/nfisher/workspace/pedestal");
        assertThat(workspace.workspaceRelative("/home/nfisher/workspace/pedestal/CLJ", "lib"), is("//:lib"));
    }

    @Test
    public void Test_workspaceAbsolute_subdir() {
        Workspace workspace = new Workspace("/home/nfisher/workspace/pedestal");
        assertThat(workspace.workspaceAbsolute("/home/nfisher/workspace/pedestal/service/CLJ", "src/**/*.clj"), is("/home/nfisher/workspace/pedestal/service/src/**/*.clj"));
    }
}