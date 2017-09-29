package ca.junctionbox.cljbuck;

import ca.junctionbox.cljbuck.channel.ReadWriterQueue;
import ca.junctionbox.cljbuck.io.FindFilesTask;
import ca.junctionbox.cljbuck.io.GlobsTask;
import ca.junctionbox.cljbuck.io.ReadFileTask;
import ca.junctionbox.cljbuck.lexer.clj.CljLex;
import ca.junctionbox.cljbuck.lexer.LexerTask;
import ca.junctionbox.cljbuck.source.SourceCache;
import ca.junctionbox.cljbuck.syntax.SyntaxTask;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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
        final long start = System.currentTimeMillis();
        final ReadWriterQueue globCh = new ReadWriterQueue();
        final ReadWriterQueue pathCh = new ReadWriterQueue();
        final ReadWriterQueue cacheCh = new ReadWriterQueue();
        final ReadWriterQueue tokenCh = new ReadWriterQueue();
        

        final SourceCache cache = SourceCache.create(logger);
        final CljLex cljLex = new CljLex();

        final int numReadFileTasks = 4;
        final int numLexerTasks = 4;
        final Callable<Object>[] allTasks = new Callable[2 + numReadFileTasks + numLexerTasks + 1];

        LogManager.getLogManager().readConfiguration(is);

        allTasks[0] = Executors.callable(new GlobsTask(args, logger, globCh));
        allTasks[1] = Executors.callable(new FindFilesTask(logger, globCh, pathCh, numReadFileTasks));

        for (int i = 2; i < numReadFileTasks + 2; i++) {
            allTasks[i] = Executors.callable(new ReadFileTask(cache, logger, pathCh, cacheCh, numLexerTasks/numReadFileTasks));
        }
        for (int i = 2 + numReadFileTasks; i < numLexerTasks + numReadFileTasks + 2; i++) {
            allTasks[i] = Executors.callable(new LexerTask(cache, logger, cljLex, cacheCh, tokenCh));
        }

        allTasks[allTasks.length - 1] = Executors.callable(new SyntaxTask(logger, numLexerTasks, tokenCh));

        final ExecutorService pool = Executors.newFixedThreadPool(allTasks.length);
        List<Future<Object>> results = Collections.emptyList();
        try {
            results = pool.invokeAll(Arrays.asList(allTasks));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final long finish = System.currentTimeMillis();

        logger.info("finished in " + (finish - start) + "ms");
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

            if(count >= 0) {
                totalGarbageCollections += count;
            }

            long time = gc.getCollectionTime();

            if(time >= 0) {
                garbageCollectionTime += time;
            }
        }

        logger.info("Total Garbage Collections: " + totalGarbageCollections);
        logger.info("Total Garbage Collection Time (ms): " + garbageCollectionTime);
    }
}

