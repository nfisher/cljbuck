package ca.junctionbox.cljbuck.build.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClojureLib extends BuildRule {
    private static final String TYPE = "ClojureLib";
    private final List<String> srcs;
    private final String ns;

    public ClojureLib(final String name, final List<String> deps, final List<String> visibility, final List<String> srcs, final String ns) {
        super(name, deps, visibility);
        this.srcs = srcs;
        this.ns = ns;
    }

    @Override
    public String getArtefact() {
        final StringBuilder sb = new StringBuilder();
        for (final String src : getSrcs()) {
            sb.append(src);
            sb.append(",");
        }
        return sb.toString();
    }

    @Override
    public List<String> getClassPaths() {
        final ArrayList<String> cps = new ArrayList<>();
        for (final String glob : srcs) {
            final int pos = glob.indexOf('*');
            if (-1 != pos) {
                final String p = glob.substring(0, pos);
                cps.add(p);
            } else {
                cps.add(glob);
            }
        }
        return cps;
    }

    @Override
    public List<String> getNamespaces() {
        return Collections.singletonList(ns);
    }

    public List<String> getSrcs() {
        return srcs;
    }
}
