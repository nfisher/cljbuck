package ca.junctionbox.cljbuck.build.commands;

import ca.junctionbox.cljbuck.build.graph.BuildGraph;
import ca.junctionbox.cljbuck.build.graph.PrintTargets;

import java.util.ArrayList;

public class PrintTargetsCommand extends Command {
    private final BuildGraph buildGraph;

    public PrintTargetsCommand(final BuildGraph buildGraph) {
        super("targets", "prints the list of buildable targets", buildGraph);
        this.buildGraph = buildGraph;
    }

    @Override
    public int exec(ArrayList<String> args) {
        this.buildGraph.forEach(new PrintTargets(System.out));
        return 0;
    }
}
