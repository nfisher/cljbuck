package ca.junctionbox.cljbuck.build.json;

import static ca.junctionbox.cljbuck.build.json.JsonKeyPair.jsonPair;

public class Event {
    public static JsonKeyPair started(final int hashcode) {
        return jsonPair()
                .add("event", "started")
                .add("hashcode", hashcode);
    }

    public static JsonKeyPair finished(final int hashcode, final long started) {
        final long finished = System.currentTimeMillis();
        return jsonPair()
                .add("event", "finished")
                .add("hashcode", hashcode)
                .add("total", finished - started);
    }
}
