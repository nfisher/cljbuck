package ca.junctionbox.cljbuck.build;

import com.google.common.collect.ImmutableMap;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;

import java.util.List;

import static ca.junctionbox.cljbuck.build.Node.Type.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class Build {
    private final String name;
    private final List<String> deps;
    private final List<String> srcs;
    private final String binaryJar;
    private final String main;
    private final List<String> visibility;
    private final Node.Type type;

    private Build(final String name, final List<String> deps, final List<String> srcs, final String binaryJar, final String main, final List<String> visibility, final Node.Type type) {
        this.name = name;
        this.deps = deps;
        this.srcs = srcs;
        this.binaryJar = binaryJar;
        this.main = main;
        this.visibility = visibility;
        this.type = type;
    }

    private Build(final String name, final Node.Type type) {
        this(name, emptyList(), emptyList(), "", "", emptyList(), type);
    }

    public static BuildGraph graph(Build... targets) throws Exception {
        final ImmutableMap.Builder<String, Node> builder = new ImmutableMap.Builder<>();
        final MutableGraph<Node> graph = GraphBuilder.directed().allowsSelfLoops(false).build();

        // add all of the nodes
        for (final Build target : targets) {
            Node node = target.build();
            builder.put(node.getName(), node);
            graph.addNode(node);
        }

        ImmutableMap<String, Node> nodeMap = builder.build();

        // add all of the edges
        for (final Node nodeV : nodeMap.values()) {
            for (final String predecessor : nodeV.getDeps()) {
                final Node nodeU = nodeMap.get(predecessor);
                graph.putEdge(nodeU, nodeV);
            }
        }

        BuildGraph buildGraph = new BuildGraph(ImmutableGraph.copyOf(graph), nodeMap);
        return buildGraph;
    }

    public static Build jar(final String name) {
        return new Build(name, Jar);
    }

    public static Build cljLib(final String name) {
        return new Build(name, Lib);
    }

    public static Build cljBinary(final String name) {
        return new Build(name, Binary);
    }

    public static Build cljTest(final String name) {
        return new Build(name, Test);
    }

    public Build visibility(final String... visiblity) {
        return new Build(name, deps, srcs, binaryJar, main, visibility, type);
    }

    public Build srcs(final String... srcs) {
        return new Build(name, deps, asList(srcs), binaryJar, main, visibility, type);
    }

    public Build main(final String main) {
        return new Build(name, deps, srcs, binaryJar, main, visibility, type);
    }

    public Build deps(final String... deps) {
        return new Build(name, asList(deps), srcs, binaryJar, main, visibility, type);
    }

    public Build binaryJar(final String binaryJar) {
        return new Build(name, deps, srcs, binaryJar, main, visibility, type);
    }

    public Node build() throws Exception {
        switch (type) {
            case Jar:
                return new Jar(name, deps, visibility, binaryJar);

            case Lib:
                return new ClojureLib(name, deps, visibility, srcs);

            case Binary:
                return new ClojureBinary(name, deps, visibility, main);

            case Test:
                return new ClojureTest(name, deps, visibility, srcs);
        }

        throw new Exception("Unknown build target type for target: " + name);
    }
}
