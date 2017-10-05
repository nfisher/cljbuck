package ca.junctionbox.cljbuck.build.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClojureTest extends BuildRule {
    private final List<String> srcs;
    private static final String TYPE = "ClojureTest";

    public ClojureTest(final String name, final List<String> deps, final List<String> visibility, final List<String> srcs) {
        super(name, deps, visibility);
        this.srcs = srcs;
    }

    @Override
    public String getArtefact() {
        return "clj test";
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
        return Collections.emptyList();
    }
}
