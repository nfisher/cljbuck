package ca.junctionbox.cljbuck.io;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.ChannelOutput;

import static ca.junctionbox.cljbuck.channel.Closer.close;

public class GlobsTask implements CSProcess {
    private final ChannelOutput<Object> out;
    private final String[] startPaths;

    public GlobsTask(final String[] startPaths, final ChannelOutput<Object> out) {
        this.startPaths = startPaths;
        this.out = out;
    }

    @Override
    public void run() {
        String clj = "**/*.clj";
        final long start = System.currentTimeMillis();
        for (String s : startPaths) {
            out.write(Glob.create(clj, s));
        }

        close(out);
        long finish = System.currentTimeMillis();
        System.out.println(this.getClass().getSimpleName() + "finished in " + (finish - start) + "ms");
    }
}
