package ca.junctionbox.cljbuck.build.commands;

import ca.junctionbox.cljbuck.build.graph.BuildGraph;
import ca.junctionbox.cljbuck.build.graph.SerialBuild;
import ca.junctionbox.cljbuck.build.rules.BuildRule;
import ca.junctionbox.cljbuck.build.runtime.ClassPath;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class BuildCommand extends Command {
    private final ClassPath classPath;
    private final String outputDir;

    public BuildCommand(final BuildGraph buildGraph, final ClassPath classPath, final String outputDir) {
        super("build", "builds the specified target", buildGraph);
        this.classPath = classPath;
        this.outputDir = outputDir;
    }

    @Override
    public int exec(final ArrayList<String> args) {
        final SerialBuild serialBuild = new SerialBuild();
        final String target = args.get(0);

        getBuildGraph().breadthFirstFrom(target, serialBuild);
        try {
            classPath.addClasspath(outputDir);
        } catch (MalformedURLException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < serialBuild.size(); i++) {
            final BuildRule rule = serialBuild.get(i);
            for (final String cp : rule.getClassPaths()) {
                try {
                    classPath.addClasspath(cp);
                } catch (MalformedURLException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            for (final String load : rule.getNamespaces()) {
                // TODO: create hierarchical/isolated outputDir for each target.
                rtLoad(outputDir, load);
            }
        }

        return 0;
    }

    public void rtLoad(final String outputDir, final String ns) {
        try {
            final Class<?> rt = classPath.forName("clojure.lang.RT", true);
            final Method rtVar = rt.getDeclaredMethod("var", String.class, String.class, Object.class);

            rtVar.invoke(null, "clojure.core", "*compile-path*", outputDir);
            rtVar.invoke(null, "clojure.core", "*compile-files*", Boolean.TRUE);

            final Method rtLoad = rt.getDeclaredMethod("load", String.class);

            rtLoad.invoke(null, ns.replace('.', '/').replace('-', '_'));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
