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

public class ReadFileTask implements Callable<Integer> {
    private final Logger logger;
    private final Reader in;
    private final Writer out;
    private final SourceCache cache;
    private final int lexers;
    private final int hashCode = hashCode();

    public ReadFileTask(final Logger logger, final Reader in, Writer out, final SourceCache cache, int lexers) {
        this.cache = cache;
        this.logger = logger;
        this.in = in;
        this.out = out;
        this.lexers = lexers;
    }

    @Override
    public Integer call() {
        logger.info(started(hashCode).toString());
        try {
            for (; ; ) {
                final Object o = in.read();
                if (receive(o)) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < lexers; i++) close(out);
        }
        logger.info(finished(hashCode).toString());
        return 0;
    }

    private boolean receive(Object o) throws IOException {
        logger.info(started(hashCode).toString());
        if (o instanceof Closer) {
            logger.info(finished(hashCode).toString());
            return true;
        }
        final Path path = (Path) o;
        cache.consume(path);
        out.write(path);
        logger.info(finished(hashCode).toString());
        return false;
    }
}
