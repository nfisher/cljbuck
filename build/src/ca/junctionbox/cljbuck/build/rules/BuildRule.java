package ca.junctionbox.cljbuck.build.rules;

import java.util.List;

public abstract class BuildRule {
    private final String name;
    private final List<String> deps;
    private final List<String> visibility;

    public BuildRule(final String name, final List<String> deps, final List<String> visibility) {
        this.name = name;
        this.deps = deps;
        this.visibility = visibility;
    }

    public abstract String getArtefact();

    public String getName() {
        return name;
    }

    public List<String> getDeps() {
        return deps;
    }

    public List<String> getVisibility() {
        return visibility;
    }

    public abstract List<String> getClassPaths();

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

    public abstract List<String> getNamespaces();

}

