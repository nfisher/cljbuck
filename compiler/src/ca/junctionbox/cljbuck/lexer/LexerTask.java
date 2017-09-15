package ca.junctionbox.cljbuck.lexer;

import ca.junctionbox.cljbuck.source.FormsTable;
import ca.junctionbox.cljbuck.source.SourceCache;
import org.jcsp.lang.AltingChannelInput;
import org.jcsp.lang.CSProcess;

import java.nio.file.Path;

public class LexerTask implements CSProcess, SourceLexer {
    private final SourceCache cache;
    private final FormsTable formsTable;

    private final AltingChannelInput in;

    public LexerTask(final SourceCache cache, FormsTable formsTable, final AltingChannelInput in) {
        this.cache = cache;
        this.formsTable = formsTable;
        this.in = in;
    }

    @Override
    public void run() {
        while(true) {
            Path p = (Path) in.read();
            if (null == p) {
                break;
            }

            cache.apply(p, this);
        }
    }

    // rip off of Rob Pikes lexer talk :D
    public void lex(final Path path, final String contents) {
        final Lexer lexer = new Lexer(path.toString(), contents);
        lexer.run();
        System.out.println(path.toString() + ": " + contents.length());
    }
}

