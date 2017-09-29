package ca.junctionbox.cljbuck.build;

import java.util.ArrayList;

public class RunCommand extends Command {
    public RunCommand(BuildGraph buildGraph) {
        super("run", "runs the specified target", buildGraph);
    }

    @Override
    public int exec(final ArrayList<String> args) {
        final BuildCommand build = new BuildCommand(getBuildGraph());
        final int rc = build.exec(args);

        if (rc != 0) {
            return rc;
        }

        return 0;
    }
}
