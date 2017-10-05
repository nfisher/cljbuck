package ca.junctionbox.cljbuck.io;

import ca.junctionbox.cljbuck.channel.Closer;
import ca.junctionbox.cljbuck.channel.Reader;
import ca.junctionbox.cljbuck.channel.Writer;
import ca.junctionbox.cljbuck.lexer.SourceCache;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import static ca.junctionbox.cljbuck.build.json.Event.finished;
import static ca.junctionbox.cljbuck.build.json.Event.started;
import static ca.junctionbox.cljbuck.channel.Closer.close;

public class ReadFileTask implements Runnable, Callable<Integer> {
    private final Logger logger;
    private final Reader in;
    private final Writer out;
    private final SourceCache cache;
    private final int lexers;

    public ReadFileTask(final Logger logger, final Reader in, Writer out, final SourceCache cache, int lexers) {
        this.cache = cache;
        this.logger = logger;
        this.in = in;
        this.out = out;
        this.lexers = lexers;
    }

    @Override
    public void run() {
        final long started = System.currentTimeMillis();
        logger.info(started(hashCode()).toString());
        long worked = 0;
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
                worked += System.currentTimeMillis() - workStart;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < lexers; i++) close(out);
        }
        logger.info(finished(hashCode(), started)
                        .add("worked", worked)
                        .toString());
    }

    @Override
    public Integer call() throws Exception {
        run();
        return 0;
    }
}
