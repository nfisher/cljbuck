package ca.junctionbox.cljbuck.build;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

abstract class BuildRule {
    private final String name;
    private final List<String> deps;
    private final List<String> visibility;

    BuildRule(final String name, final List<String> deps, List<String> visibility) {
        this.name = name;
        this.deps = deps;
        this.visibility = visibility;
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

    protected Class<?> forName(final String name, final boolean initialize) throws ClassNotFoundException {
        final Class<?> clazz = BuildRule.class;
        return Class.forName(name, initialize, clazz.getClassLoader());
    }

    protected void addClasspath(final String classPath) throws MalformedURLException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final URLClassLoader classLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        final URL jarFile = new File(classPath).toURI().toURL();

        for (final URL url : Arrays.asList(classLoader.getURLs())){
            if (url.equals(jarFile)){
                return;
            }
        }

        final Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        try {
            addUrl.setAccessible(true);
            addUrl.invoke(classLoader, jarFile);
        } finally {
            addUrl.setAccessible(false);
        }
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
