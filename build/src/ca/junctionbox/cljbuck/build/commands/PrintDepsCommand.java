package ca.junctionbox.cljbuck.build.commands;

import ca.junctionbox.cljbuck.build.graph.BuildGraph;
import ca.junctionbox.cljbuck.build.graph.PrintDepsGraph;

import java.util.ArrayList;

public class PrintDepsCommand extends Command {
    public PrintDepsCommand(final BuildGraph buildGraph) {
        super("print", "print the dependency graph for the specified target", buildGraph);
    }

    @Override
    public int exec(final ArrayList<String> args) {
        final String target = args.get(0);
        getBuildGraph().depthFirstFrom(target, new PrintDepsGraph(System.out));
        return 0;
    }
}
