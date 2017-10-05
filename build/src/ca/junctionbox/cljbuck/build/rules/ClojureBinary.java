package ca.junctionbox.cljbuck.build.rules;

import java.util.Collections;
import java.util.List;

public class ClojureBinary extends BuildRule {
    private final String main;

    public ClojureBinary(final String name, final List<String> deps, final List<String> visibility, final String main) {
        super(name, deps, visibility);
        this.main = main;
    }

    @Override
    public String getArtefact() {
        final String target = getTarget();
        return target + ".jar";
    }

    @Override
    public List<String> getClassPaths() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getNamespaces() {
        return Collections.emptyList();
    }

    public String getMain() {
        return main;
    }
}
