package ca.junctionbox.cljbuck.build;

import ca.junctionbox.cljbuck.build.commands.BuildCommand;
import ca.junctionbox.cljbuck.build.commands.Command;
import ca.junctionbox.cljbuck.build.commands.PrintDepsCommand;
import ca.junctionbox.cljbuck.build.commands.PrintTargetsCommand;
import ca.junctionbox.cljbuck.build.commands.ReplCommand;
import ca.junctionbox.cljbuck.build.commands.RunCommand;
import ca.junctionbox.cljbuck.build.graph.BuildGraph;
import ca.junctionbox.cljbuck.build.runtime.ClassPath;
import ca.junctionbox.cljbuck.channel.ReadWriterQueue;
import ca.junctionbox.cljbuck.io.FindFilesTask;
import ca.junctionbox.cljbuck.io.Glob;
import ca.junctionbox.cljbuck.io.ReadFileTask;
import ca.junctionbox.cljbuck.lexer.LexerTask;
import ca.junctionbox.cljbuck.lexer.SourceCache;
import com.google.common.collect.ImmutableSortedMap;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static ca.junctionbox.cljbuck.channel.Closer.close;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Main {
    public static final int ARG1 = 0;
    private static final int USAGE = 1;
    private static final int READER_TASKS = 4;
    private static final int LEXER_TASKS= 4;

    public static String logConfig = "handlers=java.util.logging.FileHandler\n" +
            ".level= INFO\n" +
            "java.util.logging.FileHandler.pattern=clj-out/run.log\n" +
            "\n" +
            "java.util.logging.ConsoleHandler.level = INFO\n" +
            "java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter\n" +
            "\n" +
            "java.util.logging.FileHandler.limit=10000000\n" +
            "java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter\n" +
            "java.util.logging.FileHandler.count=10\n" +
            "\n" +
            "javax.jms.connection.level = INFO\n" +
            "\n" +
            "java.util.logging.SimpleFormatter.format={\"date\":\"%1$tF\",\"time\":\"%1$tT\",\"level\":\"%4$s\",\"method\":\"%2$s\",%5$s%6$s}%n";

    public static void main(final String[] args) throws InterruptedException, ExecutionException, IOException {
        new File("clj-out").mkdirs();
        final InputStream is = new ByteArrayInputStream(logConfig.getBytes(UTF_8));
        final Logger logger = Logger.getLogger("ca.junctionbox.cljbuck.build");
        LogManager.getLogManager().readConfiguration(is);
        logger.info("\"args\": [\"" + String.join("\",\"", args) + "\"],\"event\":\"started\"");

        final Workspace workspace = new Workspace(logger).findRoot();
        final SourceCache cache = SourceCache.create(logger);
        final ReadWriterQueue globCh = new ReadWriterQueue();
        final ReadWriterQueue pathCh = new ReadWriterQueue();
        final ReadWriterQueue cacheCh = new ReadWriterQueue();
        final ReadWriterQueue tokenCh = new ReadWriterQueue();
        final ReadWriterQueue ruleCh = new ReadWriterQueue();

        globCh.write(Glob.create(workspace.getPath(), "**/CLJ"));
        close(globCh);

        final ArrayList<Callable<Integer>> tasks = new ArrayList<>();

        tasks.add(new FindFilesTask(logger, globCh, pathCh, READER_TASKS));
        for (int i = 0; i < READER_TASKS; i++) {
            tasks.add(new ReadFileTask(logger, pathCh, cacheCh, cache, LEXER_TASKS/READER_TASKS));
        }

        for (int i = 0; i < LEXER_TASKS; i++) {
            tasks.add(new LexerTask(logger, cacheCh, tokenCh, cache, new BuildFile()));
        }

        tasks.add(new RuleEmitterTask(logger, tokenCh, ruleCh, workspace, LEXER_TASKS));

        final ExecutorService pool = Executors.newFixedThreadPool(tasks.size());
        int rc = 0;
        try {
            final List<Future<Integer>> results = pool.invokeAll(tasks);

            for (final Future<Integer> f : results) {
                rc |= f.get();
            }
        } finally {
            pool.shutdown();
        }

        if (0 != rc) {
            logger.log(Level.SEVERE, "unexpected error result returned from pipeline");
            System.exit(rc);
        }

        final ArrayList<Rules> buildRules = new ArrayList<>();

        for (final Object o : ruleCh.toArray()) {
            final Rules rule = (Rules) o;
            if (null != rule) {
                buildRules.add(rule);
            }
        }

        try {
            final ClassPath cp = new ClassPath();
            final BuildGraph buildGraph = new Rules(logger, workspace, cp).graph(buildRules.toArray(new Rules[0]));

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

            rc = cmd.exec(argList);

            System.exit(rc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Composes the list of commands and initialises them with the build graph.
     *
     * @param buildGraph
     * @param classPath
     * @return
     */
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

    /**
     * Prints the applications help for available commands.
     *
     * @param out
     * @param commandList
     */
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
