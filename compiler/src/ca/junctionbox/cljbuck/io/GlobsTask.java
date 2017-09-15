package ca.junctionbox.cljbuck.io;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.ChannelOutput;

import static ca.junctionbox.cljbuck.channel.Closer.close;

public class GlobsTask implements CSProcess {
    private final ChannelOutput<Object> out;

    public GlobsTask(final ChannelOutput<Object> out) {
        this.out = out;
    }

    @Override
    public void run() {
        out.write(Glob.create("**/*.clj", "/Users/nathanfisher/workspace/mklpq/src/clj"));
        close(out);
    }
}
