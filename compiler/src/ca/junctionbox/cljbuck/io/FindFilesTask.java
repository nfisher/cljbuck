package ca.junctionbox.cljbuck.io;

import org.jcsp.lang.AltingChannelInput;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.ChannelOutput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static ca.junctionbox.cljbuck.channel.Closer.close;

public class FindFilesTask implements CSProcess {
    private final ChannelOutput<Object> out;
    private final AltingChannelInput<Object> in;

    public FindFilesTask(AltingChannelInput<Object> in, ChannelOutput<Object> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            while (true) {
                final Glob glob = (Glob) in.read();
                if (null == glob) {
                    break;
                }
                final PathTraversal pathTraversal = PathTraversal.create(glob.glob, out);
                Files.walkFileTree(Paths.get(glob.start), pathTraversal);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(out);
        }
    }
}
