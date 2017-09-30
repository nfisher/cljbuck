package ca.junctionbox.cljbuck.build.commands;

import ca.junctionbox.cljbuck.build.graph.BuildGraph;

import java.util.ArrayList;

public abstract class Command {
    private final String target;
    private final String description;
    private final BuildGraph buildGraph;

    public Command(final String target, final String description, final BuildGraph buildGraph) {
        this.target = target;
        this.description = description;
        this.buildGraph = buildGraph;
    }

    public abstract int exec(final ArrayList<String> args);

    public String getDescription() {
        return description;
    }

    public String getTarget() {
        return target;
    }

    public BuildGraph getBuildGraph() {
        return buildGraph;
    }
}
