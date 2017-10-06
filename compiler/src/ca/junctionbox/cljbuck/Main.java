package ca.junctionbox.cljbuck;

import ca.junctionbox.cljbuck.channel.ReadWriterQueue;
import ca.junctionbox.cljbuck.io.FindFilesTask;
import ca.junctionbox.cljbuck.io.GlobsTask;
import ca.junctionbox.cljbuck.io.ReadFileTask;
import ca.junctionbox.cljbuck.lexer.LexerTask;
import ca.junctionbox.cljbuck.lexer.SourceCache;
import ca.junctionbox.cljbuck.lexer.clj.CljLex;
import ca.junctionbox.cljbuck.syntax.SyntaxTask;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static ca.junctionbox.cljbuck.build.json.Event.finished;
import static ca.junctionbox.cljbuck.build.json.Event.started;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Main {
    public static String logConfig = "handlers= java.util.logging.ConsoleHandler\n" +
            ".level= INFO\n" +
            "\n" +
            "java.util.logging.ConsoleHandler.level = INFO\n" +
            "java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter\n" +
            "\n" +
            "javax.jms.connection.level = INFO\n" +
            "\n" +
            "java.util.logging.SimpleFormatter.format=%1$tF %1$tT %4$s %2$s %5$s%6$s%n";


    public static String logConfigNoDateTime = "handlers= java.util.logging.ConsoleHandler\n" +
            ".level= INFO\n" +
            "\n" +
            "java.util.logging.ConsoleHandler.level = INFO\n" +
            "java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter\n" +
            "\n" +
            "javax.jms.connection.level = INFO\n" +
            "\n" +
            "java.util.logging.SimpleFormatter.format=%4$s %2$s %5$s%6$s%n";

    public static void main(final String[] args) throws IOException {
        final InputStream is = new ByteArrayInputStream(logConfig.getBytes(UTF_8));
        final Logger logger = Logger.getLogger("ca.junctionbox.cljbuck");
        LogManager.getLogManager().readConfiguration(is);
        logger.info(started(0).toString());

        final ReadWriterQueue globCh = new ReadWriterQueue();
        final ReadWriterQueue pathCh = new ReadWriterQueue();
        final ReadWriterQueue cacheCh = new ReadWriterQueue();
        final ReadWriterQueue tokenCh = new ReadWriterQueue();

        final SourceCache cache = SourceCache.create(logger);
        final CljLex cljLex = new CljLex();

        final int numReadFileTasks = 4;
        final int numLexerTasks = 4;
        final ArrayList<Callable<Integer>> allTasks = new ArrayList<>();


        allTasks.add(new GlobsTask(args, logger, globCh));

        allTasks.add(new FindFilesTask(logger, globCh, pathCh, new File(".").getAbsolutePath(), numReadFileTasks));

        for (int i = 0; i < numReadFileTasks; i++) {
            allTasks.add(new ReadFileTask(logger, pathCh, cacheCh, cache, numLexerTasks / numReadFileTasks));
        }
        for (int i = 0; i < numLexerTasks; i++) {
            allTasks.add(new LexerTask(logger, cacheCh, tokenCh, cache, cljLex));
        }

        allTasks.add(new SyntaxTask(logger, numLexerTasks, tokenCh));

        final ExecutorService pool = Executors.newFixedThreadPool(allTasks.size());
        List<Future<Integer>> results = Collections.emptyList();
        try {
            results = pool.invokeAll(allTasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final long finish = System.currentTimeMillis();

        logger.info(finished(0).toString());
        printGCStats(logger);

        for (final Future f : results) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        pool.shutdown();
    }

    public static void printGCStats(Logger logger) {
        long totalGarbageCollections = 0;
        long garbageCollectionTime = 0;

        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {

            long count = gc.getCollectionCount();

            if (count >= 0) {
                totalGarbageCollections += count;
            }

            long time = gc.getCollectionTime();

            if (time >= 0) {
                garbageCollectionTime += time;
            }
        }

        logger.info("Total Garbage Collections: " + totalGarbageCollections);
        logger.info("Total Garbage Collection Time (ms): " + garbageCollectionTime);
    }
}

