package ca.junctionbox.cljbuck.lexer;

import ca.junctionbox.cljbuck.build.json.Tracer;
import ca.junctionbox.cljbuck.channel.Closer;
import ca.junctionbox.cljbuck.channel.Reader;
import ca.junctionbox.cljbuck.channel.Writer;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import static ca.junctionbox.cljbuck.build.json.Event.finished;
import static ca.junctionbox.cljbuck.build.json.Event.started;
import static ca.junctionbox.cljbuck.build.json.JsonKeyPair.jsonPair;
import static ca.junctionbox.cljbuck.channel.Closer.close;

public class LexerTask implements SourceLexer, Runnable, Callable<Integer> {
    private final SourceCache cache;
    private final Tracer tracer;
    private final Lexeme lexeme;

    private final Reader r;
    private final Writer w;

    public LexerTask(final Tracer tracer, final Reader r, final Writer w, final SourceCache cache, final Lexeme lexeme) {
        this.cache = cache;
        this.tracer = tracer;
        this.lexeme = lexeme;
        this.r = r;
        this.w = w;
    }

    @Override
    public void run() {
        tracer.info(started(hashCode()).toString());
        long working = 0;
        try {
            for (; ; ) {
                final Object o = r.read();

                long workStart = System.currentTimeMillis();
                if (o instanceof Closer) {
                    break;
                }
                final Path p = (Path) o;

                cache.apply(p, this);
                working += System.currentTimeMillis() - workStart;
            }
        } catch (Exception e) {
            tracer.info(e.getMessage());
        } finally {
            close(w);
        }
        tracer.info(finished(hashCode())
                .addRaw("args", jsonPair()
                        .add("working", working)
                        .toMapString())
                .toString());
    }

    public void lex(final Path path, final String contents) {
        final Lexable lexable = Lexable.create(path.toString(), contents, lexeme, w);

        tracer.info(started(hashCode())
                .addRaw("args", jsonPair()
                        .add("source", path)
                        .toMapString())
                .toString());
        lexable.run();

        tracer.info(finished(hashCode())
                .addRaw("args", jsonPair()
                                .add("source", path)
                                .add("bytes", contents.length())
                                .toMapString())
                .toString());
    }

    @Override
    public Integer call() throws Exception {
        run();
        return 0;
    }
}

