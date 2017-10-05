package ca.junctionbox.cljbuck.build.rules;

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JarTest {
    @Test
    public void Test_getClassPaths() {
        final Jar jar = new Jar("//:clojure1.9", null, null, "clojure1.9-beta1.jar");
        assertThat(jar.getClassPaths(), is(Collections.singletonList("clojure1.9-beta1.jar")));
    }

    @Test
    public void Test_getNamespaces() {
        final Jar jar = new Jar("//:clojure1.9", null, null, "clojure1.9-beta1.jar");
        assertThat(jar.getNamespaces(), is(Collections.emptyList()));
    }
}