package ca.junctionbox.cljbuck.build.json;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;

public class Tracer {
    private final Path p;
    private final ArrayBlockingQueue<String> q;
    private final Beaker beaker;
    private final Thread thread;

    private Tracer(final String outputDir) throws IOException {
        p = Paths.get(outputDir, "log-" + System.currentTimeMillis() + ".trace");
        q = new ArrayBlockingQueue<>(2048);
        beaker = new Beaker(q, p);
        thread = new Thread(beaker);
    }

    public static Tracer create(final String outputDir) {
        try {
            return new Tracer(outputDir);
        } catch (IOException e) {
            return null;
        }
    }

    public void start() {
        thread.start();
    }

    public void info(final String msg) {
        q.add(msg);
    }

    public void info(final JsonKeyPair kp) {
        q.add(kp.toString());
    }

    public void close() {
        synchronized (q) {
            q.add("]");
            while (q.isEmpty()) {
                try {
                    q.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
