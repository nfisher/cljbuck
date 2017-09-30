package ca.junctionbox.cljbuck.build.rules;

import ca.junctionbox.cljbuck.build.ClassPath;

import java.util.List;

public abstract class BuildRule {
    private final String name;
    private final List<String> deps;
    private final List<String> visibility;
    private final ClassPath cp;

    public BuildRule(final String name, final List<String> deps, final List<String> visibility, final ClassPath cp) {
        this.name = name;
        this.deps = deps;
        this.visibility = visibility;
        this.cp = cp;
    }

    public abstract void prepare();

    public abstract void build();

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

    public ClassPath getCp() {
        return cp;
    }
}

