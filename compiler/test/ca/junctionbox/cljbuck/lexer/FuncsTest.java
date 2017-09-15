package ca.junctionbox.cljbuck.lexer;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Parallel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static ca.junctionbox.cljbuck.lexer.Funcs.*;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

class Result {
    Item item;
    StateFunc fn;
}

interface Runner {
    Result run(String s);
}

public class FuncsTest {
    Runner partial(final StateFunc fn) {
        return s -> {
            final Lexer l = new Lexer("comment.clj", s);
            final Result[] actual = {new Result()};
            CSProcess receiver = () -> {
                final Item item = l.nextItem();
                actual[0].item = item;
            };

            CSProcess runner = () -> {
                actual[0].fn = fn.func(l);
            };

            new Parallel(new CSProcess[]{
                    runner,
                    receiver,
            }).run();

            return actual[0];
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
    public void Test_lexNumber() {
        Runner p = partial(lexNumeric);

        Result actual = p.run("1 ");
        assertEquals("1", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("7N)");
        assertEquals("7N", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("7N ");
        assertEquals("7N", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("2.78 ");
        assertEquals("2.78", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("017 ");
        assertEquals("017", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("-22/7 ");
        assertEquals("-22/7", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("-1.2e-5 ");
        assertEquals("-1.2e-5", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("0.2 ");
        assertEquals("0.2", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("4.2M ");
        assertEquals("4.2M", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("0xff ");
        assertEquals("0xff", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("36rCRAZY ");
        assertEquals("36rCRAZY", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("360 ");
        assertEquals("360", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("0x1.2 ");
        assertNotEquals("0x1.2", actual.item.val);
        assertNull("0x1.2 ", actual.fn);

        actual = p.run(". ");
        assertNotEquals(".", actual.item.val);
        assertNull(". ", actual.fn);

        /*
        actual = p.run("089 ");
        assertNotEquals("089", actual.item.val);
        assertNull("089 ", actual.fn);
        */
    }

    @Test
    public void Test_lexText() {
        Result actual;
        Runner p = partial(lexText);

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

    @Test(timeout=1000L)
    public void Test_Lexer_lex_single_line() {
        final Lexer l = new Lexer("comment.clj",
                "(defn hello [name] (prn \"Hola \" name))");

        ConsumeTask task = new ConsumeTask(l);

        new Parallel(new CSProcess[]{
                l,
                task,
        }).run();

        String tokens = task.items.stream()
                .filter(item -> item.type != ItemType.itemEOF)
                .map(item -> item.val)
                .reduce((a,b) -> a + " " + b)
                .get();

        assertEquals(task.items.size(), 13);
        assertEquals("( defn hello [ name ] ( prn \"Hola \" name ) )", tokens);
    }

    @Test(timeout=1000L)
    public void Test_Lexer_lex_multiple_lines() {
        final Lexer l = new Lexer("comment.clj",
                "(ns my.core)\n\n\n(defn hello [name]\n (prn \"Hola \" name))");

        ConsumeTask task = new ConsumeTask(l);

        new Parallel(new CSProcess[]{
                l,
                task,
        }).run();

        String tokens = task.items.stream()
                .filter(item -> item.type != ItemType.itemEOF)
                .map(item -> item.val)
                .reduce((a,b) -> a + " " + b)
                .get();

        assertEquals(task.items.size(), 17);
        assertEquals("( ns my.core ) ( defn hello [ name ] ( prn \"Hola \" name ) )", tokens);
    }
}

class ConsumeTask implements CSProcess {
    final Queue<Item> items = new LinkedList<>();
    final Lexer l;

    ConsumeTask(Lexer l) {
        this.l = l;
    }

    @Override
    public void run() {
        for (;;) {
            final Item item = l.nextItem();
            if (item == null) {
                break;
            }
            items.add(item);
        }
    }
}