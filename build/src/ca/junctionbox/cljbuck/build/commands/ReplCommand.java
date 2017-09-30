package ca.junctionbox.cljbuck.build.commands;

import ca.junctionbox.cljbuck.build.graph.BuildGraph;
import ca.junctionbox.cljbuck.build.ClassPath;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class ReplCommand extends Command {
    private final BuildCommand buildCommand;
    private final ClassPath cp;

    public ReplCommand(final BuildGraph buildGraph, final ClassPath cp) {
        super("repl", "start a repl session either with the current project or standalone", buildGraph);
        this.buildCommand = new BuildCommand(buildGraph);
        this.cp = cp;
    }

    @Override
    public int exec(final ArrayList<String> args) {
        buildCommand.exec(args);

        try {
            repl(args);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void repl(final ArrayList<String> args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Class<?> main = cp.forName("clojure.main", true);
        cp.printPath();

        final Method mainMain = main.getDeclaredMethod("main", new Class[]{String[].class});
        final String[] args2 = {};
        mainMain.invoke(null, new Object[]{args2});
    }
}
