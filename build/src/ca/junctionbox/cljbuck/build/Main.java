package ca.junctionbox.cljbuck.build;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static ca.junctionbox.cljbuck.build.Build.*;

public class Main {
    private static final int USAGE = 1;
    public static final int ARG1 = 0;

    public static void main(final String[] args) {
        try {
            BuildGraph buildGraph = Build.graph(
                    cljLib("//jbx:lib")
                            .srcs("src/clj/**/*.clj")
                            .deps("//lib:clojure", "//lib:guava"),

                    cljBinary("//jbx:main")
                            .main("jbx.core")
                            .deps("//jbx:lib"),

                    cljTest("//jbx:test")
                            .srcs("test/clj/**/*.clj")
                            .deps("//lib:junit"),

                    jar("//lib:guava")
                            .binaryJar("guava-23.0.jar")
                            .visibility("PUBLIC"),

                    jar("//lib:clojure")
                            .binaryJar("clojure-1.9.0-beta1.jar")
                            .visibility("PUBLIC"),

                    jar("//lib:junit")
                            .binaryJar("hamcrest-core-1.3.jar")
                            .deps("//lib:hamcrest-core")
                            .visibility("PUBLIC"),

                    jar("//lib:hamcrest-core")
                        .binaryJar("hamcrest-core-1.3.jar")
            );

            final ArrayList<String> argList = new ArrayList<>(Arrays.asList(args));
            final HashMap<String, Command> commandList = commandList(buildGraph);

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

    private static HashMap<String,Command> commandList(BuildGraph buildGraph) {
        final HashMap<String,Command> commands = new HashMap<>();
        final ArrayList<Command> list = new ArrayList<>();

        list.add(new BuildCommand(buildGraph));
        list.add(new PrintCommand(buildGraph));
        list.add(new RunCommand(buildGraph));

        for (final Command c : list) {
            commands.put(c.getTarget(), c);
        }

        return commands;
    }

    private static void printUsage(final PrintStream out, HashMap<String, Command> commandList) {
        out.println("Description:");
        out.println("  cljbuild is a clojure/jvm build too.");
        out.println("");
        out.println("Usage:");
        out.println("  cljbuild <command> [<command-options>]");
        out.println("");
        out.println("Available commands:");

        for (final Command c : commandList.values()) {
            out.print("  ");
            out.print(c.getTarget());
            out.print(" - ");
            out.println(c.getDescription());
        }

        out.println("");
    }
}

class BuildCommand extends Command {
    public BuildCommand(final BuildGraph buildGraph) {
        super("build", "builds the specified target", buildGraph);
    }

    @Override
    public int exec(final ArrayList<String> args) {
        final SerialBuild serialBuild = new SerialBuild();
        final String target = args.remove(0);

        getBuildGraph().breadthFirstFrom(target, serialBuild);
        serialBuild.build();

        return 0;
    }
}

class RunCommand extends Command {
    public RunCommand(BuildGraph buildGraph) {
        super("run", "runs the specified target", buildGraph);
    }

    @Override
    public int exec(final ArrayList<String> args) {
        final BuildCommand build = new BuildCommand(getBuildGraph());
        final int rc = build.exec(args);

        if (rc != 0) {
            return rc;
        }

        return 0;
    }
}
