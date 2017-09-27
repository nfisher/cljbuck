package ca.junctionbox.cljbuck.build;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

abstract class BuildRule {
    private final String name;
    private final List<String> deps;
    private final List<String> visibility;
    private final ArrayList<ClassLoader> loaders;

    BuildRule(final String name, final List<String> deps, List<String> visibility) {
        this.name = name;
        this.deps = deps;
        this.visibility = visibility;
        this.loaders = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<String> getDeps() {
        return deps;
    }

    public List<String> getVisibility() {
        return visibility;
    }

    @Override
    public String toString() {
        return "Node{" +
                "name='" + name + '\'' +
                ", deps=" + deps +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final BuildRule buildRule = (BuildRule) o;

        return getName() != null ? getName().equals(buildRule.getName()) : buildRule.getName() == null;
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    public abstract void build();

    public abstract String getArtefact();

    protected void addClasspath(final String classPath) throws MalformedURLException {
        System.out.println(" cp += " + classPath);

        final URL[] jar = {new File(classPath).toURI().toURL()};
        final URLClassLoader classLoader = new URLClassLoader(jar, Thread.currentThread().getContextClassLoader());

        int i = 0;
        for (ClassLoader p = classLoader.getParent(); p != null; p = classLoader.getParent()) {
           System.out.println(++i);
        }

        Thread.currentThread().setContextClassLoader(classLoader);
    }

    /**
     * Returns the directory path with trailing slash relative to the project root.
     *
     * @return path
     */
    protected String getDirectory() {
        final String name = getName();
        final int pos = name.indexOf(':');
        return name.substring(2, pos) + "/";
    }

    protected String getTarget() {
        final String name = getName();
        final int pos = name.indexOf(':') + 1;
        return name.substring(pos);
    }
}
