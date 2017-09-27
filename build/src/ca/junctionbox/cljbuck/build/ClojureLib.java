package ca.junctionbox.cljbuck.build;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.List;

public class ClojureLib extends BuildRule {
    private final List<String> srcs;

    public ClojureLib(String name, List<String> deps, List<String> visibility, List<String> srcs) {
        super(name, deps, visibility);
        this.srcs = srcs;
    }

    @Override
    public void build() {
        final File targetDir = new File("target");
        targetDir.mkdirs();
        final String glob = getArtefact();
        final int pos = glob.indexOf('*');
        try {
            addClasspath(targetDir.getPath());
            if (-1 != pos) {
                addClasspath(glob.substring(0, pos));
            }

            final Class<?> rt = forName("clojure.lang.RT", true);
            final Method rtVar = rt.getDeclaredMethod("var", String.class, String.class, Object.class);

            rtVar.invoke(null, "clojure.core", "*compile-path*", targetDir.getPath());
            rtVar.invoke(null, "clojure.core", "*compile-files*", Boolean.TRUE);

            final Method rtCompile = rt.getDeclaredMethod("compile", String.class);
            rtCompile.setAccessible(true);

            // Okie so build order is important... so need to finish the lexer...
            rtCompile.invoke(null, "jbx/pants.clj");
            rtCompile.invoke(null, "jbx/core.clj");
        } catch (ClassNotFoundException | NoSuchMethodException | MalformedURLException | IllegalAccessException | InvocationTargetException e) {
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
