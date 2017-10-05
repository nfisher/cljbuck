package ca.junctionbox.cljbuck.build.rules;

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ClojureBinaryTest {
    @Test
    public void Test_getClassPaths() {
        final ClojureBinary clojureBinary = new ClojureBinary("//:clojure1.9", null, null, null);
        assertThat(clojureBinary.getClassPaths(), is(Collections.emptyList()));
    }

    @Test
    public void Test_getNamespaces() {
        final ClojureBinary clojureBinary = new ClojureBinary("//:clojure1.9", null, null, null);
        assertThat(clojureBinary.getNamespaces(), is(Collections.emptyList()));
    }

}