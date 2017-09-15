package ca.junctionbox.cljbuck.channel;

import org.jcsp.lang.ChannelOutput;

public class Closer {
    public static void close(final ChannelOutput<Object> out) {
        out.write(null);
    }
}
