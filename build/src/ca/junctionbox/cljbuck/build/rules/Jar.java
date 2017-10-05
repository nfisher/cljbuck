package ca.junctionbox.cljbuck.build.rules;

import java.util.Collections;
import java.util.List;

public class Jar extends BuildRule {
    private final String binaryJar;
    private static final String TYPE = "jar";


    public Jar(final String name, final List<String> deps, List<String> visibility, final String binaryJar) {
        super(name, deps, visibility);
        this.binaryJar = binaryJar;
    }

    @Override
    public String getArtefact() {
        return binaryJar;
    }

    @Override
    public List<String> getClassPaths() {
        return Collections.singletonList(binaryJar);
    }

    @Override
    public List<String> getNamespaces() {
        return Collections.emptyList();
    }
}
