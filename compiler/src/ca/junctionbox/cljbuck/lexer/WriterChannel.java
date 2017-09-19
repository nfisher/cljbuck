package ca.junctionbox.cljbuck.lexer;

import org.jcsp.lang.ChannelOutput;

public class WriterChannel implements Writer {
    private final ChannelOutput<Object> out;

    public WriterChannel(final ChannelOutput<Object> out) {
        this.out = out;
    }

    @Override
    public void write(Object o) {
        out.write(o);
    }
}
