package ca.junctionbox.cljbuck.lexer;

import ca.junctionbox.cljbuck.source.SourceCache;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.ChannelInput;
import org.jcsp.lang.ChannelOutput;

import java.nio.file.Path;

import static ca.junctionbox.cljbuck.channel.Closer.close;

public class LexerTask implements CSProcess, SourceLexer {
    private final SourceCache cache;

    private final ChannelInput in;
    private final ChannelOutput<Object> out;

    public LexerTask(final SourceCache cache, final ChannelInput in, final ChannelOutput<Object> out) {
        this.cache = cache;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long working = 0;
        for (;;) {
            Path p = (Path) in.read();
            long workStart = System.currentTimeMillis();
            if (null == p) {
                break;
            }

            cache.apply(p, this);
            working += System.currentTimeMillis() - workStart;
        }
        long finish = System.currentTimeMillis();
        System.out.println(this.getClass().getSimpleName() + " finish " + (finish - start) + "ms, work " + working + "ms");
        close(out);
    }

    // rip off of Rob Pikes lexer talk :D
    public void lex(final Path path, final String contents) {
        final Lexable lexable = Lexable.create(path.toString(), contents, out);

        final long start = System.currentTimeMillis();
        lexable.run();
        final long finish = System.currentTimeMillis();
        System.out.println("\t" + path.toString() + ": " + contents.length() + " chars " + (finish - start) + "ms");
    }
}

