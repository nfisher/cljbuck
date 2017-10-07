package ca.junctionbox.cljbuck.build.json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Beaker implements Runnable {
    private final BufferedWriter os;
    private final ArrayBlockingQueue<String> q;

    Beaker(final ArrayBlockingQueue<String> q, final Path p) throws IOException {
        this.os = Files.newBufferedWriter(p, UTF_8);
        this.q = q;
    }

    @Override
    public void run() {
        try {
            boolean first = true;
            os.write("[");
            for (;;) {
                final String msg = q.take();
                if (null == msg || "]" == msg) {
                    break;
                }

                if (!first) {
                    os.write(',');
                }
                first = false;
                os.write("{");
                os.write(msg);
                os.write("}\n");
                os.flush();
            }
            os.write("]");
            os.flush();
            os.close();
            synchronized (q) {
                q.notify();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
