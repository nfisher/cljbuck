package ca.junctionbox.cljbuck.lexer;

import ca.junctionbox.cljbuck.channel.Closer;
import ca.junctionbox.cljbuck.channel.Reader;
import ca.junctionbox.cljbuck.channel.Writer;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import static ca.junctionbox.cljbuck.build.json.Event.finished;
import static ca.junctionbox.cljbuck.build.json.Event.started;
import static ca.junctionbox.cljbuck.channel.Closer.close;
import static java.util.logging.Level.SEVERE;

public class LexerTask implements SourceLexer, Runnable, Callable<Integer> {
    private final SourceCache cache;
    private final Logger logger;
    private final Lexeme lexeme;

    private final Reader r;
    private final Writer w;

    public LexerTask(final Logger logger, final Reader r, final Writer w, final SourceCache cache, final Lexeme lexeme) {
        this.cache = cache;
        this.logger = logger;
        this.lexeme = lexeme;
        this.r = r;
        this.w = w;
    }

    @Override
    public void run() {
        logger.info(started(hashCode()).toString());
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
            logger.log(SEVERE, e.getMessage());
        } finally {
            close(w);
        }
        logger.info(finished(hashCode())
                .add("working", working)
                .toString());
    }

    public void lex(final Path path, final String contents) {
        final Lexable lexable = Lexable.create(path.toString(), contents, lexeme, w);

        logger.info(started(hashCode())
                .add("source", path)
                .toString());
        lexable.run();

        logger.info(finished(hashCode())
                .add("source", path)
                .add("bytes", contents.length())
                .toString());
    }

    @Override
    public Integer call() throws Exception {
        run();
        return 0;
    }
}

