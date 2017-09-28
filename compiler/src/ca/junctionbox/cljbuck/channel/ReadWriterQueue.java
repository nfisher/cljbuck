package ca.junctionbox.cljbuck.channel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Stream;

public class ReadWriterQueue implements Writer, Reader {
    private ArrayBlockingQueue<Object> out;

    public ReadWriterQueue() {
        this.out = new ArrayBlockingQueue<>(8192);
    }

    @Override
    public void write(Object o) {
        try {
            out.put(o);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int size() {
        return out.size();
    }

    public Stream<Object> stream() {
        return out.stream();
    }

    public Object read() {
        try {
            return out.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
