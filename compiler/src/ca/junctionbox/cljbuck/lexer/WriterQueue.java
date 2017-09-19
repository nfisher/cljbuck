package ca.junctionbox.cljbuck.lexer;

import java.util.Queue;
import java.util.LinkedList;
import java.util.stream.Stream;

public class WriterQueue implements Writer, Reader {
    private Queue<Item> out;

    public WriterQueue() {
        this.out = new LinkedList<>();
    }

    @Override
    public void write(Object o) {
        out.add((Item) o);
    }

    public int size() {
        return out.size();
    }

    public Stream<Item> stream() {
        return out.stream();
    }

    public Item read() {
        return out.remove();
    }
}
