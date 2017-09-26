package ca.junctionbox.cljbuck.build;

import java.util.List;

public class Jar extends Node {
    private final String binaryJar;

    public Jar(final String name, final List<String> deps, List<String> visibility, final String binaryJar) {
        super(name, deps, visibility);
        this.binaryJar = binaryJar;
    }

    @Override
    public String getArtefact() {
        final String directory = getDirectory();
        return directory + binaryJar;
    }
}
