package ca.junctionbox.cljbuck.io;

public class Glob {
    public final String glob;
    public final String start;

    private Glob(String glob, String start) {
        this.glob = glob;
        this.start = start;
    }

    public static Glob create(final String start, final String glob) {
        return new Glob(glob, start);
    }
}
