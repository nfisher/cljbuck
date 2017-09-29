package ca.junctionbox.cljbuck.build;

import java.util.ArrayList;

public class BuildCommand extends Command {
    public BuildCommand(final BuildGraph buildGraph) {
        super("build", "builds the specified target", buildGraph);
    }

    @Override
    public int exec(final ArrayList<String> args) {
        try {
            final SerialBuild serialBuild = new SerialBuild();
            final String target = args.remove(0);

            getBuildGraph().breadthFirstFrom(target, serialBuild);

            serialBuild.prepare();

            serialBuild.build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
