package ca.junctionbox.cljbuck.build.rules;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.List;

public class ClojureLib extends BuildRule {
    private final List<String> srcs;
    private final File targetDir;
    private final String ns;

    public ClojureLib(final String name, final List<String> deps, final List<String> visibility, final List<String> srcs, final String ns) {
        super(name, deps, visibility);
        this.srcs = srcs;
        this.targetDir = new File("target");
        this.ns = ns;
    }

    @Override
    public void prepare() {
        final List<String> srcs = getSrcs();

        try {
            targetDir.mkdirs();
            addClasspath(targetDir.getPath());

            for (final String glob : srcs) {
                final int pos = glob.indexOf('*');
                if (-1 != pos) {
                    final String p = glob.substring(0, pos);
                    addClasspath(p);
                } else {
                    addClasspath(glob);
                }
            }
        } catch (MalformedURLException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void build() {
        try {
            final Class<?> rt = forName("clojure.lang.RT", true);
            final Method rtVar = rt.getDeclaredMethod("var", String.class, String.class, Object.class);

            rtVar.invoke(null, "clojure.core", "*compile-path*", targetDir.getPath());
            rtVar.invoke(null, "clojure.core", "*compile-files*", Boolean.TRUE);

            final Method rtLoad = rt.getDeclaredMethod("load", String.class);

            rtLoad.invoke(null, ns.replace('.', '/').replace('-', '_'));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
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
