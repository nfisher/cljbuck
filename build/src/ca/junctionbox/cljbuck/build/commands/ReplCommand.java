package ca.junctionbox.cljbuck.build.commands;

import ca.junctionbox.cljbuck.build.graph.BuildGraph;
import ca.junctionbox.cljbuck.build.ClassPath;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class ReplCommand extends Command {
    private final BuildCommand buildCommand;
    private final ClassPath classPath;

    public ReplCommand(final BuildGraph buildGraph, final ClassPath classPath, final BuildCommand buildCommand) {
        super("repl", "start a repl session with the target", buildGraph);
        this.buildCommand = buildCommand;
        this.classPath = classPath;
    }

    @Override
    public int exec(final ArrayList<String> args) {
        final int rc = buildCommand.exec(args);
        if (0 != rc) {
            return rc;
        }

        try {
            repl(args);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void repl(final ArrayList<String> args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Class<?> main = classPath.forName("clojure.main", true);
        classPath.printPath();

        final Method mainMain = main.getDeclaredMethod("main", new Class[]{String[].class});
        final String[] args2 = {};
        mainMain.invoke(null, new Object[]{args2});
    }
}
