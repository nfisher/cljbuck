package ca.junctionbox.cljbuck.build;

import ca.junctionbox.cljbuck.build.commands.BuildCommand;
import ca.junctionbox.cljbuck.build.commands.Command;
import ca.junctionbox.cljbuck.build.commands.PrintDepsCommand;
import ca.junctionbox.cljbuck.build.commands.PrintTargetsCommand;
import ca.junctionbox.cljbuck.build.commands.ReplCommand;
import ca.junctionbox.cljbuck.build.commands.RunCommand;
import ca.junctionbox.cljbuck.build.graph.BuildGraph;
import ca.junctionbox.cljbuck.build.json.JsonKeyPair;
import ca.junctionbox.cljbuck.build.json.Tracer;
import ca.junctionbox.cljbuck.build.runtime.ClassPath;
import ca.junctionbox.cljbuck.channel.ReadWriterQueue;
import ca.junctionbox.cljbuck.io.FindFilesTask;
import ca.junctionbox.cljbuck.io.Glob;
import ca.junctionbox.cljbuck.io.ReadFileTask;
import ca.junctionbox.cljbuck.lexer.LexerTask;
import ca.junctionbox.cljbuck.lexer.SourceCache;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static ca.junctionbox.cljbuck.build.json.Event.finished;
import static ca.junctionbox.cljbuck.build.json.Event.started;
import static ca.junctionbox.cljbuck.build.json.Event.uptime;
import static ca.junctionbox.cljbuck.build.json.JsonKeyPair.jsonPair;
import static ca.junctionbox.cljbuck.channel.Closer.close;

public class Main {
    public static final int ARG1 = 0;
    private static final int RC_USAGE = 1;
    private static final int RC_EXCEPTION = -1;
    private static final int READER_TASKS = 4;
    private static final int LEXER_TASKS= 4;
    private ExecutorService pool;
    private Tracer tracer;

    public static void main(final String[] args) {
        final JsonKeyPair mainStart = started(0).add("args", args);
        Main m = new Main();
        try {
            m.run(args, mainStart);
        } catch (InterruptedException | IOException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            m.shutdown();
        }
    }

    private void shutdown() {
        if (null != tracer) {
            tracer.close();
        }
        if (null != pool) {
            pool.shutdown();
        }
    }

    public void run(final String[] args, final JsonKeyPair mainStart) throws InterruptedException, ExecutionException, IOException {
        final Workspace workspace = new Workspace().findRoot();
        final File targetDir = new File(workspace.getOutputDir());
        targetDir.mkdirs();
        tracer = Tracer.create(workspace.getOutputDir());
        tracer.start();

        tracer.info(mainStart);

        final ArrayList<Callable<Integer>> tasks = new ArrayList<>();
        final SourceCache cache = SourceCache.create(tracer);
        final ReadWriterQueue globCh = new ReadWriterQueue();
        final ReadWriterQueue pathCh = new ReadWriterQueue();
        final ReadWriterQueue cacheCh = new ReadWriterQueue();
        final ReadWriterQueue tokenCh = new ReadWriterQueue();
        final ReadWriterQueue ruleCh = new ReadWriterQueue();

        globCh.write(Glob.create(workspace.getPath(), "**/CLJ"));
        close(globCh);


        tasks.add(new FindFilesTask(tracer, globCh, pathCh, workspace.getPath(), READER_TASKS));
        for (int i = 0; i < READER_TASKS; i++) {
            tasks.add(new ReadFileTask(tracer, pathCh, cacheCh, cache, LEXER_TASKS/READER_TASKS));
        }

        for (int i = 0; i < LEXER_TASKS; i++) {
            tasks.add(new LexerTask(tracer, cacheCh, tokenCh, cache, new BuildFile()));
        }

        tasks.add(new RuleEmitterTask(tracer, tokenCh, ruleCh, workspace, LEXER_TASKS));
        pool = Executors.newFixedThreadPool(tasks.size());

        int rc = 0;
        final List<Future<Integer>> results = pool.invokeAll(tasks);

        for (final Future<Integer> f : results) {
            rc |= f.get();
        }

        if (0 != rc) {
            exit(tracer, rc);
        }

        final ArrayList<Rules> buildRules = new ArrayList<>();

        for (final Object o : ruleCh.toArray()) {
            final Rules rule = (Rules) o;
            if (null != rule) {
                buildRules.add(rule);
            }
        }

        try {
            final ClassPath classPath = new ClassPath();
            final BuildGraph buildGraph = new Rules().graph(tracer, buildRules.toArray(new Rules[0]));

            final ArrayList<String> argList = new ArrayList<>(Arrays.asList(args));
            final ConcurrentHashMap<String, Command> commandList = commandList(tracer, buildGraph, classPath, workspace);

            if (args.length < 1) {
                printUsage(System.out, commandList);
                exit(tracer, RC_USAGE);
            }

            final String arg1 = argList.remove(ARG1);
            final Command cmd = commandList.get(arg1);

            if (null == cmd) {
                printUsage(System.out, commandList);
                exit(tracer, RC_USAGE);
            }

            rc = cmd.exec(argList);

            exit(tracer, rc);
        } catch (Exception e) {
            e.printStackTrace();
            exit(tracer, RC_EXCEPTION);
        }
    }

    public void exit(final Tracer tracer, final int rc) {
        final Runtime runtime = Runtime.getRuntime();

        final long freeMemory = runtime.freeMemory();
        final long maxMemory = runtime.maxMemory();
        final long totalMemory = runtime.totalMemory();
        final long processors = runtime.availableProcessors();

        long garbageCollections = 0;
        long garbageCollectionTime = 0;


        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            final long count = gc.getCollectionCount();

            if (count >= 0) {
                garbageCollections += count;
            }

            final long time = gc.getCollectionTime();

            if (time >= 0) {
                garbageCollectionTime += time;
            }
        }

        tracer.info(finished(0)
                .addRaw("args",
                        jsonPair()
                                .add("rc", RC_USAGE)
                                .add("numGC", garbageCollections)
                                .add("inGC", garbageCollectionTime)
                                .add("freeMemory", freeMemory)
                                .add("maxMemory", maxMemory)
                                .add("totalMemory", totalMemory)
                                .add("processors", processors)
                                .toMapString()));
        tracer.info(uptime());
        shutdown();
        System.exit(rc);
    }

    /**
     * Composes the list of commands and initialises them with the build graph.
     *
     *
     * @param logger
     * @param buildGraph
     * @param classPath
     * @param workspace
     * @return
     */
    private ConcurrentHashMap<String, Command> commandList(final Tracer logger, final BuildGraph buildGraph, final ClassPath classPath, final Workspace workspace) {
        final ConcurrentHashMap<String, Command> commands = new ConcurrentHashMap<>();
        final ArrayList<Command> list = new ArrayList<>();

        final BuildCommand buildCommand = new BuildCommand(logger, buildGraph, classPath, workspace.getOutputDir());

        list.add(buildCommand);
        list.add(new PrintDepsCommand(logger, buildGraph));
        list.add(new ReplCommand(logger, buildGraph, classPath, buildCommand));
        list.add(new RunCommand(logger, buildGraph, classPath, buildCommand));
        list.add(new PrintTargetsCommand(logger, buildGraph));

        for (final Command c : list) {
            commands.put(c.getTarget(), c);
        }

        return commands;
    }

    /**
     * Prints the applications help for available commands.
     *  @param out
     * @param commandList
     */
    private void printUsage(final PrintStream out, final ConcurrentHashMap<String, Command> commandList) {
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
