package ca.junctionbox.cljbuck.build;

import java.io.PrintStream;

import static ca.junctionbox.cljbuck.build.Build.*;

public class Main {
    private static final int USAGE = 1;

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

            if (args.length < 1) {
                printUsage(System.out);
                System.exit(USAGE);
            }

            if (buildGraph.contains(args[0])) {
                buildGraph.depthFirstFrom(args[0], new PrintGraph(System.out));
                final SerialBuild build = new SerialBuild();
                buildGraph.breadthFirstFrom(args[0], build);
                System.out.println("Build order: ");
                StringBuilder sb = new StringBuilder();
                for (Node node = build.pop(); node != null; node = build.pop()) {
                    sb.append(node.getName());
                    sb.append(", ");
                }
                System.out.println(sb.toString());
            } else {
                System.err.println(args[0] + " is not a known build target.");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void printUsage(final PrintStream out) {
        out.println("Description:");
        out.println("  cljbuild is a clojure/jvm build too.");
        out.println("");
        out.println("Usage:");
        out.println("  cljbuild <command> [<command-options>]");
        out.println("");
        out.println("Available commands:");
        out.println("");
    }
}

/*
class JavaLibrary extends Node {
    private final List<String> srcs;

    public JavaLibrary(final String name, final List<String> deps, final List<String> srcs) {
        super(name, deps, visibility);
        this.srcs = srcs;
    }
}

class PrebuiltJar extends Node {
    private final String binaryJar;
    private final List<String> visibility;

    public PrebuiltJar(final String name, final List<String> deps, final String binaryJar, final List<String> visibility) {
        super(name, deps, visibility);
        this.binaryJar = binaryJar;
        this.visibility = visibility;
    }
}
*/
