package ca.junctionbox.cljbuck.lexer;

import ca.junctionbox.cljbuck.channel.Closer;
import ca.junctionbox.cljbuck.channel.Reader;
import ca.junctionbox.cljbuck.channel.Writer;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import static ca.junctionbox.cljbuck.channel.Closer.close;
import static java.util.logging.Level.SEVERE;

public class LexerTask implements SourceLexer, Runnable, Callable<Integer> {
    private final SourceCache cache;
    private final Logger logger;
    private final Lexeme cljLex;

    private final Reader r;
    private final Writer w;

    public LexerTask(final Logger logger, final Reader r, final Writer w, final SourceCache cache, final Lexeme lexeme) {
        this.cache = cache;
        this.logger = logger;
        this.cljLex = lexeme;
        this.r = r;
        this.w = w;
    }

    @Override
    public void run() {
        logger.info("\"event\":\"started\"");
        long start = System.currentTimeMillis();
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
        final long finish = System.currentTimeMillis();
        logger.info("\"event\":\"finished\",\"working\":" + working + ",\"total\":" + (finish - start));
    }

    public void lex(final Path path, final String contents) {
        final Lexable lexable = Lexable.create(path.toString(), contents, cljLex, w);

        logger.info("\"event\":\"started\",\"source\":\"" + path.toString() + "\"");
        final long start = System.currentTimeMillis();
        lexable.run();
        final long finish = System.currentTimeMillis();
        logger.info("\"event\":\"finished\",\"source\":\"" + path.toString() + "\",\"total\":" + (finish - start) +",\"bytes\":" + contents.length());
    }

    @Override
    public Integer call() throws Exception {
        run();
        return 0;
    }
}

