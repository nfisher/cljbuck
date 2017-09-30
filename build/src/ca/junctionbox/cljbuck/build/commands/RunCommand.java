package ca.junctionbox.cljbuck.build.commands;

import ca.junctionbox.cljbuck.build.ClassPath;
import ca.junctionbox.cljbuck.build.graph.BuildGraph;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class RunCommand extends Command {
    private final BuildCommand buildCommand;
    private final ClassPath classPath;

    public RunCommand(final BuildGraph buildGraph, final ClassPath classPath, final BuildCommand buildCommand) {
        super("run", "runs the specified target", buildGraph);
        this.classPath = classPath;
        this.buildCommand = buildCommand;
    }

    @Override
    public int exec(final ArrayList<String> args) {
        final int rc = buildCommand.exec(args);

        if (rc != 0) {
            return rc;
        }

        try {
            run(args);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void run(final ArrayList<String> args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Class<?> main = classPath.forName("jbx.core", true);

        final Method mainMain = main.getDeclaredMethod("main", new Class[]{String[].class});
        final String[] args2 = {};
        mainMain.invoke(null, new Object[]{args2});
    }
}
