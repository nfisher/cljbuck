package ca.junctionbox.cljbuck.build;

import ca.junctionbox.cljbuck.build.graph.BuildGraph;
import ca.junctionbox.cljbuck.build.rules.BuildRule;
import ca.junctionbox.cljbuck.build.rules.ClojureBinary;
import ca.junctionbox.cljbuck.build.rules.ClojureLib;
import ca.junctionbox.cljbuck.build.rules.ClojureTest;
import ca.junctionbox.cljbuck.build.rules.Jar;
import ca.junctionbox.cljbuck.build.rules.Type;
import ca.junctionbox.cljbuck.build.runtime.ClassPath;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ca.junctionbox.cljbuck.build.rules.Type.*;
import static ca.junctionbox.cljbuck.build.rules.Type.Jar;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class Rules {
    final String binaryJar;
    final List<String> deps;
    final String main;
    final String name;
    final String ns;
    final List<String> srcs;
    final Type type;
    final List<String> visibility;
    final ClassPath cp;
    final Workspace workspace;

    private Rules(final String name,
                  final List<String> deps,
                  final List<String> srcs,
                  final String binaryJar,
                  final String main,
                  final List<String> visibility,
                  final Type type,
                  final String ns,
                  final ClassPath cp, final Workspace workspace) {
        this.name = name;
        this.deps = deps;
        this.srcs = srcs;
        this.binaryJar = binaryJar;
        this.main = main;
        this.visibility = visibility;
        this.type = type;
        this.ns = ns;
        this.cp = cp;
        this.workspace = workspace;
    }

    private Rules(final Type type) {
        this("", emptyList(), emptyList(), "", "", emptyList(), type, "", null, null);
    }

    public Rules(final Workspace workspace, final ClassPath cp) {
        this("", emptyList(), emptyList(), "", "", emptyList(), null, "", cp, workspace);
    }

    public static Rules jar() {
        return new Rules(Jar);
    }

    public static Rules cljLib() {
        return new Rules(CljLib);
    }

    public static Rules cljBinary() {
        return new Rules(CljBinary);
    }

    public static Rules cljTest() {
        return new Rules(CljTest);
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

    public Rules name(final String name) {
        return new Rules(name, deps, srcs, binaryJar, main, visibility, type, ns, cp, workspace);
    }

    public Rules visibility(final String... visiblity) {
        return new Rules(name, deps, srcs, binaryJar, main, visibility, type, ns, cp, workspace);
    }

    public Rules srcs(final String... srcs) {
        return new Rules(name, deps, asList(srcs), binaryJar, main, visibility, type, ns, cp, workspace);
    }

    public Rules main(final String main) {
        return new Rules(name, deps, srcs, binaryJar, main, visibility, type, ns, cp, workspace);
    }

    public Rules deps(final String... deps) {
        return new Rules(name, asList(deps), srcs, binaryJar, main, visibility, type, ns, cp, workspace);
    }

    public Rules binaryJar(final String binaryJar) {
        return new Rules(name, deps, srcs, binaryJar, main, visibility, type, ns, cp, workspace);
    }

    public Rules ns(final String ns) {
        return new Rules(name, deps, srcs, binaryJar, main, visibility, type, ns, cp, workspace);
    }

    public Rules appendDep(final String dep) {
        final ArrayList<String> deps = new ArrayList<>();
        for (String s : this.deps) {
            deps.add(s);
        }
        deps.add(dep);
        return new Rules(name, deps, srcs, binaryJar, main, visibility, type, ns, cp, workspace);
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

    public Rules appendVisibility(final String v) {
        final ArrayList<String> visibility = new ArrayList<>();
        for (final String s : this.visibility) {
            visibility.add(s);
        }
        visibility.add(v);
        return new Rules(name, deps, srcs, binaryJar, main, visibility, type, ns, cp, workspace);
    }

    public Rules appendSrc(final String v) {
        final ArrayList<String> srcs = new ArrayList<>();
        for (final String s : this.srcs) {
            srcs.add(s);
        }
        srcs.add(v);
        return new Rules(name, deps, srcs, binaryJar, main, visibility, type, ns, cp, workspace);
    }
}
