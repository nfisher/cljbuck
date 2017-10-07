package ca.junctionbox.cljbuck.build.commands;

import ca.junctionbox.cljbuck.build.graph.BuildGraph;
import ca.junctionbox.cljbuck.build.json.Tracer;

import java.util.ArrayList;

public abstract class Command {
    private final String target;
    private final String description;
    private final BuildGraph buildGraph;
    private final Tracer tracer;

    public Command(final Tracer tracer, final String target, final String description, final BuildGraph buildGraph) {
        this.tracer = tracer;
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

    public Tracer getTracer() {
        return tracer;
    }
}
