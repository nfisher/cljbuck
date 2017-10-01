package ca.junctionbox.cljbuck.build;

import ca.junctionbox.cljbuck.build.commands.*;
import ca.junctionbox.cljbuck.build.graph.BuildGraph;
import com.google.common.collect.ImmutableSortedMap;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.logging.Logger;

import static ca.junctionbox.cljbuck.build.Rules.*;

public class Main {
    public static final int ARG1 = 0;
    private static final int USAGE = 1;

    public static void main(final String[] args) {
        final Logger logger = Logger.getLogger("ca.junctionbox.cljbuck.build");

        try {
            final ClassPath cp = new ClassPath();
            final BuildGraph buildGraph = new Rules(cp).graph(
                    cljLib("//jbx:lib")
                            .srcs("src/clj/", "src/cljc/")
                            .ns("jbx.core")
                            .deps("//lib:clojure1.9"),

                    cljBinary("//jbx:main")
                            .main("jbx.core")
                            .deps("//jbx:lib"),

                    cljTest("//jbx:test")
                            .srcs("test/clj/**/*.clj"),

                    jar("//lib:clojure1.9")
                            .binaryJar("clojure-1.9.0-beta1.jar")
                            .deps("//lib:core.specs.alpha", "//lib:spec.alpha")
                            .visibility("PUBLIC"),

                    jar("//lib:clojure1.8")
                            .binaryJar("clojure-1.8.0.jar")
                            .visibility("PUBLIC"),

                    jar("//lib:spec.alpha")
                            .binaryJar("spec.alpha-0.1.123.jar"),

                    jar("//lib:core.specs.alpha")
                            .binaryJar("core.specs.alpha-0.1.24.jar")
            );

            final ArrayList<String> argList = new ArrayList<>(Arrays.asList(args));
            final ImmutableSortedMap<String, Command> commandList = commandList(buildGraph, cp);

            if (args.length < 1) {
                printUsage(System.out, commandList);
                System.exit(USAGE);
            }

            final String arg1 = argList.remove(ARG1);
            final Command cmd = commandList.get(arg1);

            if (null == cmd) {
                printUsage(System.out, commandList);
                System.exit(USAGE);
            }

            int rc = cmd.exec(argList);

            System.exit(rc);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static ImmutableSortedMap<String, Command> commandList(final BuildGraph buildGraph, final ClassPath classPath) {
        final ImmutableSortedMap.Builder<String, Command> commands = new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());
        final ArrayList<Command> list = new ArrayList<>();

        final BuildCommand buildCommand = new BuildCommand(buildGraph);
        list.add(buildCommand);
        list.add(new PrintDepsCommand(buildGraph));
        list.add(new ReplCommand(buildGraph, classPath, buildCommand));
        list.add(new RunCommand(buildGraph, classPath, buildCommand));
        list.add(new PrintTargetsCommand(buildGraph));

        for (final Command c : list) {
            commands.put(c.getTarget(), c);
        }

        return commands.build();
    }

    private static void printUsage(final PrintStream out, final SortedMap<String, Command> commandList) {
        out.println("Description:");
        out.println("  cljbuild is a clojure/jvm build too.");
        out.println("");
        out.println("Usage:");
        out.println("  cljbuild <command> [<command-options>]");
        out.println("");
        out.println("Available commands:");

        for (final Command c : commandList.values()) {
            final String target = c.getTarget();
            final int padLen = 14 - target.length();
            final char[] padding = new char[padLen];
            Arrays.fill(padding, ' ');

            out.print("  ");
            out.print(target);
            out.print(padding);
            out.print(" ");
            out.println(c.getDescription());
        }

        out.println("");
    }
}

