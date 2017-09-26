package ca.junctionbox.cljbuck.build;

import com.google.common.collect.ImmutableMap;
import com.google.common.graph.ImmutableGraph;

import java.io.PrintStream;
import java.util.*;

public class BuildGraph {
    final ImmutableGraph<Node> graph;
    final ImmutableMap<String, Node> map;

    BuildGraph(final ImmutableGraph<Node> graph, final ImmutableMap<String, Node> map) {
        this.graph = graph;
        this.map = map;
    }

    public int size() {
        return map.size();
    }

    public boolean contains(final String name) {
        return map.get(name) != null;
    }

    public Set<Node> successors(final String name) throws NotFoundException {
        final Node node = map.get(name);
        if (node == null) {
            throw new NotFoundException(name);
        }
        return graph.successors(node);
    }

    public Set<Node> predecessors(final String name) throws NotFoundException {
        final Node node = map.get(name);
        if (node == null) {
            throw new NotFoundException(name);
        }
        return graph.predecessors(node);
    }

    public void depthFirstFrom(final String start, final Walken christopher) {
        final Stack<Node> nodes = new Stack<>();
        final Set<String> seen = new HashSet<>();
        final Node startNode = map.get(start);
        int depth = 0;

        nodes.push(map.get(start));
        seen.add(startNode.getName());
        christopher.step(startNode, depth);

        while (!nodes.isEmpty()) {
            final Node parent = nodes.peek();
            final Node child = unseenChild(parent, seen);

            if (null != child) {
                seen.add(child.getName());
                christopher.step(child, depth);
                nodes.push(child);
            } else {
                nodes.pop();
            }
        }
    }

    public void breadthFirstFrom(final String start, final Walken christopher) {
        final Queue<Node> nodes = new LinkedList<>();
        final Node startNode = map.get(start);
        int depth = 0;

        nodes.add(startNode);
        christopher.step(startNode, depth);

        while(!nodes.isEmpty()) {
            final Node parent = nodes.remove();
            depth++;
            final Set<Node> children = graph.predecessors(parent);
            for (final Node child : children) {
                christopher.step(child, depth);
                nodes.add(child);
            }
        }
    }

    private Node unseenChild(final Node parent, final Set<String> seen) {
        final Set<Node> children = graph.predecessors(parent);
        Node child = null;

        for (final Node n : children) {
            if (seen.contains(n.getName())) {
                continue;
            }

            child = n;
            break;
        }

        return child;
    }
}

interface Walken {
    void step(final Node node, final int depth);
}
