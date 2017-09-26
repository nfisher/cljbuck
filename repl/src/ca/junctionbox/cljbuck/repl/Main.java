package ca.junctionbox.cljbuck.repl;

import clojure.lang.RT;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

public class Main {
    public static void main(final String[] main) throws IOException {
        long start = System.nanoTime();

        RT.var("clojure.core", "+").invoke(1, 2, 3);

        long finish = System.nanoTime();
        System.out.println((finish - start));
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

