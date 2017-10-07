package ca.junctionbox.cljbuck.io;

import ca.junctionbox.cljbuck.build.json.Tracer;
import ca.junctionbox.cljbuck.channel.Writer;

import java.util.concurrent.Callable;

import static ca.junctionbox.cljbuck.build.json.Event.finished;
import static ca.junctionbox.cljbuck.build.json.Event.started;
import static ca.junctionbox.cljbuck.channel.Closer.close;

public class GlobsTask implements Runnable, Callable<Integer> {
    private final String[] startPaths;
    private final Tracer tracer;
    private final Writer out;

    public GlobsTask(final String[] startPaths, final Tracer tracer, final Writer out) {
        this.startPaths = startPaths;
        this.tracer = tracer;
        this.out = out;
    }

    @Override
    public void run() {
        tracer.info(started(hashCode()).toString());
        final String clj = "**/*.clj";

        for (String s : startPaths) {
            out.write(Glob.create(s, clj));
        }

        close(out);

        tracer.info(finished(hashCode()).toString());
    }

    @Override
    public Integer call() throws Exception {
        run();
        return 0;
    }
}
