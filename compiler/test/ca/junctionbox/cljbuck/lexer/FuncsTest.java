package ca.junctionbox.cljbuck.lexer;

import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.Funcs.*;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

class Result {
    Item item;
    StateFunc fn;
}

interface Runner {
    Result run(String s);
}

public class FuncsTest {
    public static Runner partial(final StateFunc fn) {
        return s -> {
            final WriterQueue q = new WriterQueue();
            final Lexable l = Lexable.create("comment.clj", s, q);
            final Result result = new Result();

            result.fn = fn.func(l);
            result.item = q.read();

            return result;
        };
    }

    @Test
    public void Test_lexComment() {
        Runner p = partial(lexComment);

        Result actual = p.run("; hello world\n(");
        assertEquals("; hello world\n", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("; hello world");
        assertEquals("; hello world", actual.item.val);
        assertNotNull(actual.fn);
    }

    @Test
    public void Test_lexString() {
        Runner p = partial(lexString);

        Result actual = p.run("\"hello\nworld\" ");
        assertEquals("\"hello\nworld\"", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("\"hello\nworld\"\n(");
        assertEquals("\"hello\nworld\"", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("\"hello\nworld");
        assertEquals("unclosed string", actual.item.val);
        assertNull(actual.fn);
    }

    @Test
    public void Test_lexKeyword() {
        Runner p = partial(lexKeyword);
        Result actual = p.run(":simple ");
        assertEquals(":simple", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run(":ns/k ");
        assertEquals(":ns/k", actual.item.val);
        assertNotNull(actual.fn);
    }

    @Test
    public void Test_lexSymbol() {
        Runner p = partial(lexSymbol);

        Result actual = p.run("*hello-world-123* ");
        assertEquals("*hello-world-123*", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("hello/world ");
        assertEquals("hello/world", actual.item.val);
        assertNotNull(actual.fn);
    }

    @Test
    public void Test_lexFile() {
        Result actual;
        Runner p = partial(lexFile);

        actual = p.run("(prn hello)");
        assertEquals("(", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("   \n\n(ns cc.jbx");
        assertEquals("(", actual.item.val);
        assertNotNull(actual.fn);
    }

    @Test
    public void Test_lexText() {
        Result actual;
        Runner p = partial(lexForm);

        actual = p.run("(hello");
        assertEquals("(", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("[hello");
        assertEquals("[", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("{hello");
        assertEquals("{", actual.item.val);
        assertNotNull(actual.fn);
    }

}
