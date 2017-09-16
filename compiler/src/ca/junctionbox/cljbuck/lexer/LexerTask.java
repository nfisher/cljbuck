package ca.junctionbox.cljbuck.lexer;

import ca.junctionbox.cljbuck.source.FormsTable;
import ca.junctionbox.cljbuck.source.SourceCache;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.ChannelInput;
import org.jcsp.lang.Parallel;

import java.nio.file.Path;

public class LexerTask implements CSProcess, SourceLexer {
    private final SourceCache cache;
    private final FormsTable formsTable;

    private final ChannelInput in;

    public LexerTask(final SourceCache cache, FormsTable formsTable, final ChannelInput in) {
        this.cache = cache;
        this.formsTable = formsTable;
        this.in = in;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long working = 0;
        while(true) {
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
    }

    // rip off of Rob Pikes lexer talk :D
    public void lex(final Path path, final String contents) {
        final Lexable lexable = new Lexer(path.toString(), contents);
        final ConsumeTask task = new ConsumeTask(lexable);

        long start = System.currentTimeMillis();
        new Parallel(new CSProcess[]{
                lexable,
                task,
        }).run();
        long finish = System.currentTimeMillis();
        System.out.println("\t" + path.toString() + ": " + contents.length() + " chars lexed " + task.items.size() + " symbols " + (finish - start) + "ms");
    }
}

