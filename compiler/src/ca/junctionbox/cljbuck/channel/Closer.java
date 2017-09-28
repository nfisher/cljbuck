package ca.junctionbox.cljbuck.channel;

public class Closer {
    public static void close(final Writer w) {
        w.write(new Closer());
    }
}
