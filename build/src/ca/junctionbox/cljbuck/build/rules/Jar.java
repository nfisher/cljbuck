package ca.junctionbox.cljbuck.build.rules;

import ca.junctionbox.cljbuck.build.rules.BuildRule;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.List;

public class Jar extends BuildRule {
    private final String binaryJar;

    public Jar(final String name, final List<String> deps, List<String> visibility, final String binaryJar) {
        super(name, deps, visibility);
        this.binaryJar = binaryJar;
    }

    @Override
    public void prepare() {
        try {
            addClasspath(getArtefact());
        } catch (final MalformedURLException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void build() {
    }

    @Override
    public String getArtefact() {
        final String directory = getDirectory();
        return directory + binaryJar;
    }
}
