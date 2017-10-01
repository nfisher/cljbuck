package ca.junctionbox.cljbuck.io;

import ca.junctionbox.cljbuck.channel.Closer;
import ca.junctionbox.cljbuck.channel.Reader;
import ca.junctionbox.cljbuck.channel.Writer;
import ca.junctionbox.cljbuck.source.SourceCache;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import static ca.junctionbox.cljbuck.channel.Closer.close;

public class ReadFileTask implements Runnable {
    private final SourceCache cache;
    private final Logger logger;
    private final Reader in;
    private final Writer out;
    private final int lexers;

    public ReadFileTask(final SourceCache cache, final Logger logger, final Reader in, Writer out, int lexers) {
        this.cache = cache;
        this.logger = logger;
        this.in = in;
        this.out = out;
        this.lexers = lexers;
    }

    @Override
    public void run() {
        logger.info("started");
        final long start = System.currentTimeMillis();
        long working = 0;
        try {
            for (; ; ) {
                final Object o = in.read();
                long workStart = System.currentTimeMillis();
                if (o instanceof Closer) {
                    break;
                }
                final Path path = (Path) o;
                cache.consume(path);
                out.write(path);
                working += System.currentTimeMillis() - workStart;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < lexers; i++) close(out);
        }
        final long finish = System.currentTimeMillis();
        logger.info("finished in " + (finish - start) + "ms, work " + working + "ms");
    }
}
