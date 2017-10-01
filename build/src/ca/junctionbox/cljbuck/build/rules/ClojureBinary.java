package ca.junctionbox.cljbuck.build.rules;

import ca.junctionbox.cljbuck.build.ClassPath;

import java.util.List;

public class ClojureBinary extends BuildRule {
    private final String main;

    public ClojureBinary(final String name, final List<String> deps, final List<String> visibility, final String main, ClassPath cp) {
        super(name, deps, visibility, cp);
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
        return target + ".jar";
    }

    public String getMain() {
        return main;
    }
}
