package ca.junctionbox.cljbuck.build.rules;

import java.util.List;

public class ClojureTest extends BuildRule {
    private final List<String> srcs;

    public ClojureTest(final String name, final List<String> deps, final List<String> visibility, final List<String> srcs, ClassPath cp) {
        super(name, deps, visibility, cp);
        this.srcs = srcs;
    }

    @Override
    public void prepare() {

    }

    @Override
    public void build() {

    }

    @Override
    public String getArtefact() {
        return "clj test";
    }
}
