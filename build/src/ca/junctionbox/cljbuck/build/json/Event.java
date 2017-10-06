package ca.junctionbox.cljbuck.build.json;

import static ca.junctionbox.cljbuck.build.json.JsonKeyPair.jsonPair;

public class Event {
    public static final String EVENT_TYPE = "ph";
    public static final String BEGIN = "B";
    public static final String END = "E";
    public static final String THREAD_ID = "tid";
    public static final String PROCESS_ID = "pid";

    public static JsonKeyPair started(final int hashcode) {
        return jsonPair()
                .add(EVENT_TYPE, BEGIN)
                .add(THREAD_ID, Thread.currentThread().getId())
                .add(PROCESS_ID, 0);
    }

    public static JsonKeyPair finished(final int hashcode) {
        return jsonPair()
                .add(EVENT_TYPE, END)
                .add(THREAD_ID, Thread.currentThread().getId())
                .add(PROCESS_ID, 0);
    }
}
