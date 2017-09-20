package ca.junctionbox.cljbuck;

import ca.junctionbox.cljbuck.io.FindFilesTask;
import ca.junctionbox.cljbuck.io.GlobsTask;
import ca.junctionbox.cljbuck.io.ReadFileTask;
import ca.junctionbox.cljbuck.lexer.LexerTask;
import ca.junctionbox.cljbuck.source.FormsTable;
import ca.junctionbox.cljbuck.source.SourceCache;
import ca.junctionbox.cljbuck.syntax.SyntaxTask;
import org.jcsp.lang.*;
import org.jcsp.util.Buffer;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

public class Main {
    public static void main(final String[] args) throws IOException {
        long start = System.currentTimeMillis();
        final One2OneChannel<Object> globCh = Channel.one2one(new Buffer(2048), 0);
        final One2AnyChannel<Object> pathCh = Channel.one2any(new Buffer(2048),0);
        final One2AnyChannel<Object> cacheCh = Channel.one2any(new Buffer(2048),0);
        final Any2OneChannel<Object> tokenCh = Channel.any2one(new Buffer(2048),0);

        final SourceCache cache = SourceCache.create();

        int numReadFileTasks = 4;
        int numLexerTasks = 16;
        CSProcess[] allTasks = new CSProcess[2 + numReadFileTasks + numLexerTasks + 1];

        allTasks[0] = new GlobsTask(args, globCh.out());
        allTasks[1] = new FindFilesTask(globCh.in(), pathCh.out(), numReadFileTasks);

        for (int i = 2; i < numReadFileTasks + 2; i++) allTasks[i] = new ReadFileTask(cache, pathCh.in(), cacheCh.out(), numLexerTasks/numReadFileTasks);
        for (int i = 2 + numReadFileTasks; i < numLexerTasks + numReadFileTasks + 2; i++) allTasks[i] = new LexerTask(cache, cacheCh.in(), tokenCh.out());

        allTasks[allTasks.length - 1] = new SyntaxTask(tokenCh.in());

        new Parallel(allTasks).run();

        long finish = System.currentTimeMillis();
        System.out.println("finished in " + (finish - start) + "ms");

        printGCStats();
    }

    public static void printGCStats() {
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

        System.out.println("Total Garbage Collections: " + totalGarbageCollections);
        System.out.println("Total Garbage Collection Time (ms): " + garbageCollectionTime);
    }
}

