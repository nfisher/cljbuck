package ca.junctionbox.cljbuck.build.rules;

import java.util.List;

public class ClojureBinary extends BuildRule {
    private final String main;

    public ClojureBinary(final String name, final List<String> deps, final List<String> visibility, final String main) {
        super(name, deps, visibility);
        this.main = main;
    }

    @Override
    public void prepare() {

    }

    @Override
    public void build() {

    }

    @Override
    public String getArtefact() {
        final String target = getTarget();
        return  target + ".jar";
    }
}
