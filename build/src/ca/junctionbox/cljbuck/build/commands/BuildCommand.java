package ca.junctionbox.cljbuck.build.commands;

import ca.junctionbox.cljbuck.build.graph.BuildGraph;
import ca.junctionbox.cljbuck.build.graph.SerialBuild;

import java.util.ArrayList;

public class BuildCommand extends Command {
    public BuildCommand(final BuildGraph buildGraph) {
        super("build", "builds the specified target", buildGraph);
    }

    @Override
    public int exec(final ArrayList<String> args) {
        final SerialBuild serialBuild = new SerialBuild();
        final String target = args.get(0);

        getBuildGraph().breadthFirstFrom(target, serialBuild);

        serialBuild.prepare();

        serialBuild.build();

        return 0;
    }
}
