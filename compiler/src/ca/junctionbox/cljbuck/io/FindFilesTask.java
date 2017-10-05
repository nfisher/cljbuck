package ca.junctionbox.cljbuck.io;

import ca.junctionbox.cljbuck.channel.Closer;
import ca.junctionbox.cljbuck.channel.Reader;
import ca.junctionbox.cljbuck.channel.Writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import static ca.junctionbox.cljbuck.build.json.Event.finished;
import static ca.junctionbox.cljbuck.build.json.Event.started;
import static ca.junctionbox.cljbuck.channel.Closer.close;

public class FindFilesTask implements Runnable, Callable<Integer> {
    private final Logger logger;
    private final Reader in;
    private final Writer out;
    private final int readers;

    public FindFilesTask(final Logger logger, final Reader in, final Writer out, final int readers) {
        this.logger = logger;
        this.in = in;
        this.out = out;
        this.readers = readers;
    }

    @Override
    public void run() {
        logger.info(started(hashCode()).toString());
        boolean first = true;
        long start = 0;
        try {
            while (true) {
                final Object o = in.read();
                if (first) {
                    first = false;
                    start = System.currentTimeMillis();
                }
                if (o instanceof Closer) {
                    break;
                }
                final Glob glob = (Glob) o;
                final PathTraversal pathTraversal = PathTraversal.create(glob.glob, out);
                // TODO: Look at using JNI find . -name CLJ is done 54ms where this PoS is done in 300ms.
                Files.walkFileTree(Paths.get(glob.start), pathTraversal);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < readers; i++) close(out);
            logger.info(finished(hashCode(), start).toString());
        }
    }

    @Override
    public Integer call() throws Exception {
        run();
        return 0;
    }
}
