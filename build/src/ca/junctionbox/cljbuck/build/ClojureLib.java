package ca.junctionbox.cljbuck.build;

import java.util.List;

public class ClojureLib extends BuildRule {
    private final List<String> srcs;

    public ClojureLib(String name, List<String> deps, List<String> visibility, List<String> srcs) {
        super(name, deps, visibility);
        this.srcs = srcs;
    }

    @Override
    public void build() {

    }

    @Override
    public String getArtefact() {
        final StringBuilder sb = new StringBuilder();
        for (final String src : getSrcs()) {
            sb.append(getDirectory());
            sb.append(src);
            sb.append(",");
        }
        return sb.toString();
    }

    public List<String> getSrcs() {
        return srcs;
    }
}
