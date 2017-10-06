package ca.junctionbox.cljbuck.build.commands;

import ca.junctionbox.cljbuck.build.graph.BuildGraph;

import java.util.ArrayList;
import java.util.logging.Logger;

public abstract class Command {
    private final String target;
    private final String description;
    private final BuildGraph buildGraph;
    private final Logger logger;

    public Command(final Logger logger, final String target, final String description, final BuildGraph buildGraph) {
        this.logger = logger;
        this.description = description;
        this.buildGraph = buildGraph;
        this.target = target;
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

    public Logger getLogger() {
        return logger;
    }
}
