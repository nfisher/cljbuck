package ca.junctionbox.cljbuck.build.commands;

import ca.junctionbox.cljbuck.build.graph.BuildGraph;
import ca.junctionbox.cljbuck.build.json.Tracer;
import ca.junctionbox.cljbuck.build.runtime.ClassPath;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class RunCommand extends Command {
    private final BuildCommand buildCommand;
    private final ClassPath classPath;

    public RunCommand(Tracer logger, final BuildGraph buildGraph, final ClassPath classPath, final BuildCommand buildCommand) {
        super(logger, "run", "runs the specified target", buildGraph);
        this.classPath = classPath;
        this.buildCommand = buildCommand;
    }

    @Override
    public int exec(final ArrayList<String> args) {
        if (args.size() != 1) {
            System.out.println("Argh matey");
            return 1;
        }
        final int rc = buildCommand.exec(args);

        if (rc != 0) {
            return rc;
        }

        try {
            return run(args);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return 3;
    }

    public int run(final ArrayList<String> args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String ns = getBuildGraph().mainFor(args.get(0));
        if (null == ns || ns.equals("")) {
            return 2;
        }
        final Class<?> main = classPath.forName(ns, true);

        final Method mainMain = main.getDeclaredMethod("main", new Class[]{String[].class});
        final String[] args2 = {};
        mainMain.invoke(null, new Object[]{args2});
        return 0;
    }
}
