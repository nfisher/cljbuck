package ca.junctionbox.cljbuck;

import ca.junctionbox.cljbuck.io.FindFilesTask;
import ca.junctionbox.cljbuck.io.ReadFileTask;
import ca.junctionbox.cljbuck.lexer.LexerTask;
import ca.junctionbox.cljbuck.io.GlobsTask;
import ca.junctionbox.cljbuck.source.FormsTable;
import ca.junctionbox.cljbuck.source.SourceCache;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Channel;
import org.jcsp.lang.One2OneChannel;
import org.jcsp.lang.Parallel;
import org.jcsp.util.Buffer;

import java.io.IOException;

public class Main {
    public static void main(final String[] args) throws IOException {
        final One2OneChannel<Object> globCh = Channel.one2one(new Buffer(10), 0);
        final One2OneChannel<Object> pathCh = Channel.one2one(new Buffer(10),0);
        final One2OneChannel<Object> cacheCh = Channel.one2one(new Buffer(10),0);

        final SourceCache cache = SourceCache.create();
        final FormsTable forms = FormsTable.create();

        new Parallel(new CSProcess[]{
                new GlobsTask(globCh.out()),
                new FindFilesTask(globCh.in(), pathCh.out()),
                new ReadFileTask(cache, pathCh.in(), cacheCh.out()),
                new LexerTask(cache, forms, cacheCh.in()),
        }).run();
    }
}

