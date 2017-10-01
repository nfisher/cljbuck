package ca.junctionbox.cljbuck.build;

import ca.junctionbox.cljbuck.build.graph.BuildGraph;
import ca.junctionbox.cljbuck.build.rules.*;
import ca.junctionbox.cljbuck.build.rules.Jar;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;

import java.util.Comparator;
import java.util.List;

import static ca.junctionbox.cljbuck.build.Type.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class Rules {
    private final String binaryJar;
    private final List<String> deps;
    private final String main;
    private final String name;
    private final String ns;
    private final List<String> srcs;
    private final Type type;
    private final List<String> visibility;
    private final ClassPath cp;

    private Rules(final String name,
                  final List<String> deps,
                  final List<String> srcs,
                  final String binaryJar,
                  final String main,
                  final List<String> visibility,
                  final Type type,
                  final String ns,
                  final ClassPath cp) {
        this.name = name;
        this.deps = deps;
        this.srcs = srcs;
        this.binaryJar = binaryJar;
        this.main = main;
        this.visibility = visibility;
        this.type = type;
        this.ns = ns;
        this.cp = cp;
    }

    private Rules(final String name, final Type type) {
        this(name, emptyList(), emptyList(), "", "", emptyList(), type, "", null);
    }

    public Rules(final ClassPath cp) {
        this("", emptyList(), emptyList(), "", "", emptyList(), null, "", cp);
    }

    public static Rules jar(final String name) {
        return new Rules(name, Jar);
    }

    public static Rules cljLib(final String name) {
        return new Rules(name, CljLib);
    }

    public static Rules cljBinary(final String name) {
        return new Rules(name, CljBinary);
    }

    public static Rules cljTest(final String name) {
        return new Rules(name, CljTest);
    }

    public BuildGraph graph(Rules... targets) throws Exception {
        final ImmutableSortedMap.Builder<String, BuildRule> builder = new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());
        final MutableGraph<BuildRule> graph = GraphBuilder.directed().allowsSelfLoops(false).build();

        // add all of the nodes
        for (final Rules target : targets) {
            final BuildRule buildRule = target.build(cp);
            builder.put(buildRule.getName(), buildRule);
            graph.addNode(buildRule);
        }

        final ImmutableSortedMap<String, BuildRule> nodeMap = builder.build();

        // add all of the edges
        for (final BuildRule buildRuleV : nodeMap.values()) {
            for (final String predecessor : buildRuleV.getDeps()) {
                final BuildRule buildRuleU = nodeMap.get(predecessor);
                graph.putEdge(buildRuleU, buildRuleV);
            }
        }

        final BuildGraph buildGraph = new BuildGraph(ImmutableGraph.copyOf(graph), nodeMap);
        return buildGraph;
    }

    public Rules visibility(final String... visiblity) {
        return new Rules(name, deps, srcs, binaryJar, main, visibility, type, ns, cp);
    }

    public Rules srcs(final String... srcs) {
        return new Rules(name, deps, asList(srcs), binaryJar, main, visibility, type, ns, cp);
    }

    public Rules main(final String main) {
        return new Rules(name, deps, srcs, binaryJar, main, visibility, type, ns, cp);
    }

    public Rules deps(final String... deps) {
        return new Rules(name, asList(deps), srcs, binaryJar, main, visibility, type, ns, cp);
    }

    public Rules binaryJar(final String binaryJar) {
        return new Rules(name, deps, srcs, binaryJar, main, visibility, type, ns, cp);
    }

    public Rules ns(final String ns) {
        return new Rules(name, deps, srcs, binaryJar, main, visibility, type, ns, cp);
    }

    public BuildRule build(final ClassPath cp) throws Exception {
        switch (type) {
            case Jar:
                return new Jar(name, deps, visibility, binaryJar, cp);

            case CljLib:
                return new ClojureLib(name, deps, visibility, srcs, ns, cp);

            case CljBinary:
                return new ClojureBinary(name, deps, visibility, main, cp);

            case CljTest:
                return new ClojureTest(name, deps, visibility, srcs, cp);
        }

        throw new Exception("Unknown build target type for target: " + name);
    }
}
