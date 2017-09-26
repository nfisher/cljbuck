package ca.junctionbox.cljbuck.build;

import java.util.List;

public class ClojureBinary extends Node {
    private final String main;

    public ClojureBinary(final String name, final List<String> deps, final List<String> visibility, final String main) {
        super(name, deps, visibility);
        this.main = main;
    }

    @Override
    public String getArtefact() {
        final String target = getTarget();
        return  target + ".jar";
    }
}
