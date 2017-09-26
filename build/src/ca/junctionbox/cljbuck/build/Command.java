package ca.junctionbox.cljbuck.build;

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

    abstract int exec(final ArrayList<String> args);

    String getDescription() {
        return description;
    }

    public String getTarget() {
        return target;
    }

    public BuildGraph getBuildGraph() {
        return buildGraph;
    }
}
