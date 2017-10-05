package ca.junctionbox.cljbuck.build.rules;

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ClojureLibTest {
    @Test
    public void Test_getClassPaths() {
        final ClojureLib clojureLib = new ClojureLib("//:lib", null, null, Collections.singletonList("/lib/**/*.clj"), null);
        assertThat(clojureLib.getClassPaths(), is(Collections.singletonList("/lib/")));
    }

    @Test
    public void Test_getNamespaces() {
        final ClojureLib clojureLib = new ClojureLib("//:lib", null, null, Collections.singletonList("/lib/**/*.clj"), "jbx.core");
        assertThat(clojureLib.getNamespaces(), is(Collections.singletonList("jbx.core")));
    }
}