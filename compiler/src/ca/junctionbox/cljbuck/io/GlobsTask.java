package ca.junctionbox.cljbuck.io;

import ca.junctionbox.cljbuck.channel.Writer;

import java.util.logging.Logger;

import static ca.junctionbox.cljbuck.channel.Closer.close;

public class GlobsTask implements Runnable {
    private final String[] startPaths;
    private final Logger logger;
    private final Writer out;

    public GlobsTask(final String[] startPaths, final Logger logger, final Writer out) {
        this.startPaths = startPaths;
        this.logger = logger;
        this.out = out;
    }

    @Override
    public void run() {
        logger.info("started");
        final String clj = "**/*.clj";
        final long start = System.currentTimeMillis();

        for (String s : startPaths) {
            out.write(Glob.create(clj, s));
        }

        close(out);

        final long finish = System.currentTimeMillis();

        logger.info("finished in " + (finish - start) + "ms");
    }
}
