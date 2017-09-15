package ca.junctionbox.cljbuck.io;

import ca.junctionbox.cljbuck.source.SourceCache;
import org.jcsp.lang.AltingChannelInput;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.ChannelOutput;
import org.jcsp.lang.PoisonException;

import java.io.IOException;
import java.nio.file.Path;

import static ca.junctionbox.cljbuck.channel.Closer.close;

public class ReadFileTask implements CSProcess {
    private final SourceCache cache;
    private final AltingChannelInput<Object> in;
    private final ChannelOutput out;

    public ReadFileTask(final SourceCache cache, final AltingChannelInput<Object> in, ChannelOutput out) {
        this.cache = cache;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            while(true) {
                final Path path = (Path) in.read();
                if (null == path) {
                    break;
                }
                cache.consume(path);
                out.write(path);
            }
        } catch (PoisonException pe) {
            System.out.println("FileReadTask received poison from in channel.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(out);
        }
    }
}
