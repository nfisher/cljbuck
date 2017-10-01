package ca.junctionbox.cljbuck.build;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestIntegration {
    @Test
    public void Test_fail() {
        assertThat("tests written: you've been a very naughty boy...", 0, is(0));
    }
}
