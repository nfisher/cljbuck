package ca.junctionbox.cljbuck.build.json;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import static ca.junctionbox.cljbuck.build.json.JsonKeyPair.jsonPair;

public class Event {
    private static final String BEGIN = "B";
    private static final String COMPLETE = "X";
    private static final String DURATION = "dur";
    private static final String END = "E";
    private static final String EVENT_TYPE = "ph";
    private static final String PROCESS_ID = "pid";
    private static final String THREAD_ID = "tid";
    private static final String TIMESTAMP = "ts";
    private static final String NAME = "name";

    public static JsonKeyPair started(final int hashcode) {
        return jsonPair()
                .add(TIMESTAMP, System.currentTimeMillis() * 1000)
                .add(NAME, getName())
                .add(EVENT_TYPE, BEGIN)
                .add(THREAD_ID, Thread.currentThread().getId())
                .add(PROCESS_ID, 0);
    }

    public static JsonKeyPair finished(final int hashcode) {
        return jsonPair()
                .add(NAME, getName())
                .add(TIMESTAMP, System.currentTimeMillis() * 1000)
                .add(EVENT_TYPE, END)
                .add(THREAD_ID, Thread.currentThread().getId())
                .add(PROCESS_ID, 0);
    }

    public static JsonKeyPair uptime() {
        final RuntimeMXBean mgmt = ManagementFactory.getRuntimeMXBean();
        return jsonPair()
                .add(NAME, "java -jar")
                .add(TIMESTAMP, mgmt.getStartTime() * 1000)
                .add(EVENT_TYPE, COMPLETE)
                .add(THREAD_ID, Thread.currentThread().getId())
                .add(DURATION, mgmt.getUptime() * 1000)
                .add(PROCESS_ID, 0);
    }

    public static String getName() {
        final Thread t = Thread.currentThread();
        final StackTraceElement stack = t.getStackTrace()[3];
        return stack.getClassName() + " " + stack.getMethodName();
    }
}
