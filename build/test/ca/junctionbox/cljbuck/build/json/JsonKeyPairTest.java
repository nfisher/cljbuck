package ca.junctionbox.cljbuck.build.json;

import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JsonKeyPairTest {
    @Test
    public void Test_integer_value() {
        final JsonKeyPair kp = new JsonKeyPair();
        kp.add("int", 1);
        assertThat(kp.toString(), is("\"int\":1"));
    }

    @Test
    public void Test_long_value() {
        final JsonKeyPair kp = new JsonKeyPair();
        kp.add("long", 1L);
        assertThat(kp.toString(), is("\"long\":1"));
    }

    @Test
    public void Test_string_value() {
        final JsonKeyPair kp = new JsonKeyPair();
        kp.add("str", "ohai");
        assertThat(kp.toString(), is("\"str\":\"ohai\""));
    }

    @Test
    public void Test_multiple_pairs() {
        final JsonKeyPair kp = new JsonKeyPair();
        kp.add("str", "ohai")
                .add("long", 1L);
        assertThat(kp.toString(), is("\"str\":\"ohai\",\"long\":1"));
    }

    @Test
    public void Test_int_array() {
        final JsonKeyPair kp = new JsonKeyPair();
        kp.add("array", new int[]{1, 2});
        assertThat(kp.toString(), is("\"array\":[1,2]"));
    }

    @Test
    public void Test_long_array() {
        final JsonKeyPair kp = new JsonKeyPair();
        kp.add("array", new long[]{1L, 2L});
        assertThat(kp.toString(), is("\"array\":[1,2]"));
    }

    @Test
    public void Test_arrayList() {
        final ArrayList<String> a = new ArrayList<>();
        a.add("Hola");
        a.add("Hello");
        final JsonKeyPair kp = new JsonKeyPair();
        kp.add("array", a);
        assertThat(kp.toString(), is("\"array\":[\"Hola\",\"Hello\"]"));
    }

    @Test
    public void Test_string_array() {
        final JsonKeyPair kp = new JsonKeyPair();
        kp.add("array", new String[]{"Hola", "Hello"});
        assertThat(kp.toString(), is("\"array\":[\"Hola\",\"Hello\"]"));
    }
}