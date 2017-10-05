package ca.junctionbox.cljbuck.build.rules;

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ClojureTestTest {
    @Test
    public void Test_getClassPaths() {
        final ClojureTest clojureTest = new ClojureTest("//:lib", null, null, Collections.singletonList("/test/**/*.clj"));
        assertThat(clojureTest.getClassPaths(), is(Collections.singletonList("/test/")));
    }

    @Test
    public void Test_getNamespaces() {
        final ClojureTest clojureTest = new ClojureTest("//:lib", null, null, Collections.singletonList("/test/**/*.clj"));
        assertThat(clojureTest.getNamespaces(), is(Collections.emptyList()));
    }
}