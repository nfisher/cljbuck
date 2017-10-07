package ca.junctionbox.cljbuck.build.json;

import java.util.Collection;

public class JsonKeyPair {
    private final StringBuilder stringBuilder;

    public static JsonKeyPair jsonPair() {
        return new JsonKeyPair();
    }

    public JsonKeyPair() {
        this.stringBuilder = new StringBuilder();
    }

    private void quoteHug(final Object key) {
        // TODO (NF 2017-10-05): Apply JSON escapes (e.g. " and \). I'm ok with sharp edges ATM.
        stringBuilder.append('"').append(key).append('"');
    }

    public JsonKeyPair add(final String key, final int i) {
        quoteHug(key);
        stringBuilder
                .append(':')
                .append(i)
                .append(',');
        return this;
    }

    public JsonKeyPair add(final String key, final long[] a) {
        quoteHug(key);

        stringBuilder
                .append(':')
                .append('[');

        boolean first = true;
        for (final long i : a) {
            if (!first) {
                stringBuilder.append(',');
            }
            stringBuilder.append(i);
            first = false;
        }

        stringBuilder
                .append(']')
                .append(',');

        return this;
    }

    public JsonKeyPair add(final String key, final Collection col) {
        quoteHug(key);
        stringBuilder
                .append(':')
                .append('[');

        boolean first = true;
        for (Object o : col) {
            if (!first) {
                stringBuilder.append(',');
            }
            quoteHug(o);
            first = false;
        }
        stringBuilder
                .append(']')
                .append(',');
        return this;
    }

    public JsonKeyPair add(final String key, final int[] a) {
        quoteHug(key);
        stringBuilder
                .append(':')
                .append('[');

        boolean first = true;
        for (final int i : a) {
            if (!first) {
                stringBuilder.append(',');
            }
            stringBuilder.append(i);
            first = false;
        }

        stringBuilder
                .append(']')
                .append(',');

        return this;
    }

    public JsonKeyPair add(final String key, final Object[] a) {
        quoteHug(key);
        stringBuilder
                .append(':')
                .append('[');

        boolean first = true;
        for (final Object o : a) {
            if (!first) {
                stringBuilder.append(',');
            }
            quoteHug(o);
            first = false;
        }

        stringBuilder
                .append(']')
                .append(',');

        return this;
    }

    public JsonKeyPair add(final String key, final long l) {
        quoteHug(key);
        stringBuilder
                .append(':')
                .append(l)
                .append(',');
        return this;
    }

    public JsonKeyPair add(final String key, final Object value) {
        quoteHug(key);
        stringBuilder.append(':');
        quoteHug(value);
        stringBuilder.append(',');
        return this;
    }

    public JsonKeyPair addRaw(final String key, final String value) {
        quoteHug(key);
        stringBuilder.append(':');
        stringBuilder.append(value);
        stringBuilder.append(',');

        return this;
    }

    public String toString() {
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    public String toMapString() {
        return "{" + toString() + "}";
    }
}
