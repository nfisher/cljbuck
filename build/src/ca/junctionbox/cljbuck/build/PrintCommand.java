package ca.junctionbox.cljbuck.build;

import java.util.ArrayList;

class PrintCommand extends Command {
    public PrintCommand(final BuildGraph buildGraph) {
        super("print", "print the dependency graph for the specified target", buildGraph);
    }

    @Override
    int exec(final ArrayList<String> args) {
        final String target = args.get(0);
        getBuildGraph().depthFirstFrom(target, new PrintGraph(System.out));
        return 0;
    }
}
