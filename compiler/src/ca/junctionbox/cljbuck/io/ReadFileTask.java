package ca.junctionbox.cljbuck.io;

import ca.junctionbox.cljbuck.build.json.Tracer;
import ca.junctionbox.cljbuck.channel.Closer;
import ca.junctionbox.cljbuck.channel.Reader;
import ca.junctionbox.cljbuck.channel.Writer;
import ca.junctionbox.cljbuck.lexer.SourceCache;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static ca.junctionbox.cljbuck.build.json.Event.finished;
import static ca.junctionbox.cljbuck.build.json.Event.started;
import static ca.junctionbox.cljbuck.channel.Closer.close;

public class ReadFileTask implements Callable<Integer> {
    private final Tracer tracer;
    private final Reader in;
    private final Writer out;
    private final SourceCache cache;
    private final int lexers;
    private final int hashCode = hashCode();

    public ReadFileTask(final Tracer tracer, final Reader in, Writer out, final SourceCache cache, int lexers) {
        this.cache = cache;
        this.tracer = tracer;
        this.in = in;
        this.out = out;
        this.lexers = lexers;
    }

    @Override
    public Integer call() {
        tracer.info(started(hashCode).toString());
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
        tracer.info(finished(hashCode).toString());
        return 0;
    }

    private boolean receive(Object o) throws IOException {
        tracer.info(started(hashCode).toString());
        if (o instanceof Closer) {
            tracer.info(finished(hashCode).toString());
            return true;
        }
        final Path path = (Path) o;
        cache.consume(path);
        out.write(path);
        tracer.info(finished(hashCode).toString());
        return false;
    }
}
