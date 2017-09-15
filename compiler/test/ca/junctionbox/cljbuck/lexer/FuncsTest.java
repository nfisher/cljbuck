package ca.junctionbox.cljbuck.lexer;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Parallel;
import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.Funcs.*;
import static org.junit.Assert.*;

interface Runner {
    String run(String s);
}

public class FuncsTest {
    Runner partial(final StateFunc fn) {
        return s -> {
            final Lexer l = new Lexer("comment.clj", s);
            final String[] actual = {""};
            CSProcess receiver = () -> {
                final Item commentItem = l.nextItem();
                actual[0] = commentItem.val;
            };

            CSProcess runner = () -> {
                fn.func(l);
            };

            new Parallel(
                    new CSProcess[]{
                            runner,
                            receiver,
                    }
            ).run();

            return actual[0];
        };
    }

    @Test
    public void Test_lexComment() {
        Runner p = partial(lexComment);
        String actual = p.run("; hello world\n(");
        assertEquals("; hello world\n", actual);

        actual = p.run("; hello world");
        assertEquals("; hello world", actual);
    }

    @Test
    public void Test_lexString() {
        Runner p = partial(lexString);
        String actual = p.run("\"hello\nworld\"\n(");
        assertEquals("\"hello\nworld\"", actual);

        actual = p.run("\"hello\nworld");
        assertEquals("unclosed string", actual);
    }

    @Test
    public void Test_lexKeyword() {
        Runner p = partial(lexKeyword);
        String actual = p.run(":simple ");
        assertEquals(":simple", actual);

        actual = p.run(":ns/k ");
        assertEquals(":ns/k", actual);
    }

    @Test
    public void Test_lexSymbol() {
        Runner p = partial(lexSymbol);

        String actual = p.run("*hello-world-123* ");
        assertEquals("*hello-world-123*", actual);

        actual = p.run("hello/world ");
        assertEquals("hello/world", actual);
    }

    @Test
    public void Test_lexNumber() {
        Runner p = partial(lexNumeric);

        String actual = p.run("1 ");
        assertEquals("1", actual);

        actual = p.run("7N ");
        assertEquals("7N", actual);

        actual = p.run("2.78 ");
        assertEquals("2.78", actual);

        actual = p.run("017 ");
        assertEquals("017", actual);

        actual = p.run("-22/7 ");
        assertEquals("-22/7", actual);

        actual = p.run("-1.2e-5 ");
        assertEquals("-1.2e-5", actual);

        actual = p.run("0.2 ");
        assertEquals("0.2", actual);

        actual = p.run("4.2M ");
        assertEquals("4.2M", actual);

        actual = p.run("0xff ");
        assertEquals("0xff", actual);

        actual = p.run("36rCRAZY ");
        assertEquals("36rCRAZY", actual);

        actual = p.run("360 ");
        assertEquals("360", actual);

        actual = p.run("0x1.2 ");
        assertNotEquals("0x1.2", actual);

        actual = p.run(". ");
        assertNotEquals(".", actual);

        actual = p.run("089 ");
        assertNotEquals("089", actual);
    }
}