package ca.junctionbox.cljbuck.build;

import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class WorkspaceTest {

    @Test
    public void Test_workspaceRelative_subdir() {
        Logger logger = Logger.getLogger("abc");
        logger.setLevel(Level.OFF);
        Workspace workspace = new Workspace(logger, "/home/nfisher/workspace/pedestal");
        assertThat(workspace.workspaceRelative("/home/nfisher/workspace/pedestal/lib/CLJ", "lib"), is("//lib:lib"));
    }

    @Test
    public void Test_workspaceRelative_root() {
        Logger logger = Logger.getLogger("abc");
        logger.setLevel(Level.OFF);
        Workspace workspace = new Workspace(logger, "/home/nfisher/workspace/pedestal");
        assertThat(workspace.workspaceRelative("/home/nfisher/workspace/pedestal/CLJ", "lib"), is("//:lib"));
    }
}