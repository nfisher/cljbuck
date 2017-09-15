package ca.junctionbox.cljbuck.io;

import ca.junctionbox.cljbuck.source.SourceCache;
import org.jcsp.lang.*;

import java.io.IOException;
import java.nio.file.Path;

import static ca.junctionbox.cljbuck.channel.Closer.close;

public class ReadFileTask implements CSProcess {
    private final SourceCache cache;
    private final ChannelInput<Object> in;
    private final ChannelOutput out;
    private final int lexers;

    public ReadFileTask(final SourceCache cache, final ChannelInput<Object> in, ChannelOutput out, int lexers) {
        this.cache = cache;
        this.in = in;
        this.out = out;
        this.lexers = lexers;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long working = 0;
        try {
            while(true) {
                final Path path = (Path) in.read();
                long workStart = System.currentTimeMillis();
                if (null == path) {
                    break;
                }
                cache.consume(path);
                out.write(path);
                working += System.currentTimeMillis() - workStart;
            }
        } catch (PoisonException pe) {
            System.out.println("FileReadTask received poison from in channel.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < lexers; i++) close(out);
        }
        long finish = System.currentTimeMillis();
        System.out.println(this.getClass().getSimpleName() + " finish " + (finish - start) + "ms, work " + working + "ms");
    }
}
