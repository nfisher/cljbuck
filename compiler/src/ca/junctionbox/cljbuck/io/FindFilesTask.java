package ca.junctionbox.cljbuck.io;

import org.jcsp.lang.AltingChannelInput;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.ChannelInput;
import org.jcsp.lang.ChannelOutput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static ca.junctionbox.cljbuck.channel.Closer.close;

public class FindFilesTask implements CSProcess {
    private final ChannelOutput<Object> out;
    private final ChannelInput<Object> in;
    private final int readers;

    public FindFilesTask(ChannelInput<Object> in, ChannelOutput<Object> out, int readers) {
        this.in = in;
        this.out = out;
        this.readers = readers;
    }

    @Override
    public void run() {
        boolean first = true;
        long start = 0;
        try {
            while (true) {
                final Glob glob = (Glob) in.read();
                if (first) {
                    first = false;
                    start = System.currentTimeMillis();
                }
                if (null == glob) {
                    break;
                }
                final PathTraversal pathTraversal = PathTraversal.create(glob.glob, out);
                Files.walkFileTree(Paths.get(glob.start), pathTraversal);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < readers; i++) close(out);
            long finish = System.currentTimeMillis();
            System.out.println(this.getClass().getSimpleName() + " finish " + (finish - start) + "ms");
        }
    }
}
