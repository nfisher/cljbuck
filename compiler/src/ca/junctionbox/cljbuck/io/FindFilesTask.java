package ca.junctionbox.cljbuck.io;

import ca.junctionbox.cljbuck.build.json.Tracer;
import ca.junctionbox.cljbuck.channel.Closer;
import ca.junctionbox.cljbuck.channel.Reader;
import ca.junctionbox.cljbuck.channel.Writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import static ca.junctionbox.cljbuck.build.json.Event.finished;
import static ca.junctionbox.cljbuck.build.json.Event.started;
import static ca.junctionbox.cljbuck.channel.Closer.close;

public class FindFilesTask implements Runnable, Callable<Integer> {
    private final Tracer tracer;
    private final Reader in;
    private final Writer out;
    private final int readers;
    private final int hashCode = hashCode();
    private final String workspacePath;

    public FindFilesTask(final Tracer tracer, final Reader in, final Writer out, final String workspacePath, final int readers) {
        this.tracer = tracer;
        this.in = in;
        this.out = out;
        this.readers = readers;
        this.workspacePath = workspacePath;
    }

    @Override
    public void run() {
        tracer.info(started(hashCode).toString());
        try {
            while (true) {
                final Object o = in.read();
                if (receive(o)) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < readers; i++) close(out);
            tracer.info(finished(hashCode).toString());
        }
    }

    private boolean receive(final Object o) throws IOException {
        tracer.info(started(hashCode).toString());
        if (o instanceof Closer) {
            tracer.info(finished(hashCode).toString());
            return true;
        }
        final Glob glob = (Glob) o;
        final PathTraversal pathTraversal = PathTraversal.create(glob.glob, out, workspacePath);
        Files.walkFileTree(Paths.get(glob.start), pathTraversal);
        tracer.info(finished(hashCode).toString());
        return false;
    }

    @Override
    public Integer call() throws Exception {
        run();
        return 0;
    }
}
