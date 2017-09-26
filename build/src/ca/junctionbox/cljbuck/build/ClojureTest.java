package ca.junctionbox.cljbuck.build;

import java.util.List;

public class ClojureTest extends Node {
    private final List<String> srcs;

    ClojureTest(final String name, final List<String> deps, final List<String> visibility, final List<String> srcs) {
        super(name, deps, visibility);
        this.srcs = srcs;
    }

    @Override
    public String getArtefact() {
        return "clj test";
    }
}
