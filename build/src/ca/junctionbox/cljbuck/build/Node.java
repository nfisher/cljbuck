package ca.junctionbox.cljbuck.build;

import java.util.List;

abstract class Node {
    private final String name;
    private final List<String> deps;
    private final List<String> visibility;

    Node(final String name, final List<String> deps, List<String> visibility) {
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

        final Node node = (Node) o;

        return getName() != null ? getName().equals(node.getName()) : node.getName() == null;
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    public abstract String getArtefact();

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

    public enum Type {
        Lib,
        Jar,
        Binary,
        Test,
    }
}
