package ca.junctionbox.cljbuck.build.graph;

import ca.junctionbox.cljbuck.build.rules.BuildRule;
import ca.junctionbox.cljbuck.build.rules.ClojureBinary;
import com.google.common.graph.MutableGraph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class BuildGraph {
    private final MutableGraph<BuildRule> graph;
    private final ConcurrentHashMap<String, BuildRule> map;

    public BuildGraph(final MutableGraph<BuildRule> graph, final ConcurrentHashMap<String, BuildRule> map) {
        this.graph = graph;
        this.map = map;
    }

    public int size() {
        return map.size();
    }

    public boolean contains(final String name) {
        return map.get(name) != null;
    }

    public Set<BuildRule> successors(final String name) throws NotFoundException {
        final BuildRule buildRule = map.get(name);
        if (buildRule == null) {
            throw new NotFoundException(name);
        }
        return graph.successors(buildRule);
    }

    public Set<BuildRule> predecessors(final String name) throws NotFoundException {
        final BuildRule buildRule = map.get(name);
        if (buildRule == null) {
            throw new NotFoundException(name);
        }
        return graph.predecessors(buildRule);
    }

    public void forEach(final Walken christopher) {
        for (final String key : map.keySet()) {
            christopher.step(map.get(key), 0);
        }
    }

    public void depthFirstFrom(final String start, final Walken christopher) {
        final Stack<BuildRule> buildRules = new Stack<>();
        final Set<String> seen = new HashSet<>();
        final BuildRule startRule = map.get(start);

        buildRules.push(map.get(start));
        seen.add(startRule.getName());
        christopher.step(startRule, 0);

        while (!buildRules.isEmpty()) {
            final BuildRule parent = buildRules.peek();
            final BuildRule child = unseenChild(parent, seen);

            if (null != child) {
                seen.add(child.getName());
                christopher.step(child, buildRules.size());
                buildRules.push(child);
            } else {
                buildRules.pop();
            }
        }
    }

    public void breadthFirstFrom(final String start, final Walken christopher) {
        final Queue<BuildRule> buildRules = new LinkedList<>();
        final BuildRule startRule = map.get(start);
        int depth = 0;

        buildRules.add(startRule);
        christopher.step(startRule, depth);

        while (!buildRules.isEmpty()) {
            final BuildRule parent = buildRules.remove();
            depth++;
            final Set<BuildRule> children = graph.predecessors(parent);
            for (final BuildRule child : children) {
                christopher.step(child, depth);
                buildRules.add(child);
            }
        }
    }

    private BuildRule unseenChild(final BuildRule parent, final Set<String> seen) {
        final Set<BuildRule> children = graph.predecessors(parent);
        BuildRule child = null;

        for (final BuildRule n : children) {
            if (seen.contains(n.getName())) {
                continue;
            }

            child = n;
            break;
        }

        return child;
    }

    public String mainFor(final String nodeName) {
        final ClojureBinary clojureBinary = (ClojureBinary) map.get(nodeName);
        if (null == clojureBinary) {
            return "";
        }

        return clojureBinary.getMain();
    }
}
