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

    @Test(timeout = 100L)
    public void Test_lexNumber_with_invalid_numbers() {
        Runner p = partial(lexNumeric);

        String[] testData = {
                "360T"
               // "2.7.8",
        };

        for (String td  : testData) {
            final Result actual = p.run(td);
            assertEquals("NumberFormatException Invalid number", actual.item.val);
            assertNull(actual.fn);
        }
    }

    @Test(timeout = 100L)
    public void Test_lexNumber_with_valid_numbers() {
        Runner p = partial(lexNumeric);

        String[][] testData = {
                {"360 ", "360", "itemLong"},
                {"2.78 ", "2.78", "itemDouble"},
                {"-360 ", "-360", "itemLong"},
                {"-2.78 ", "-2.78", "itemDouble"},
                {"0xff ", "0xff", "itemLong"},
                {"077 ", "077", "itemLong"},
                {"-1.2e-5 ", "-1.2e-5", "itemDouble"},
                {"-22/7 ", "-22/7", "itemRatio"},
                {"0.78 ", "0.78", "itemRatio"},
                {"4.2M ", "4.2M", "itemBigDecimal"},
                {"7N ", "7N", "itemBigInt"},

                /*
                {"36rCRAZY ", "36rCRAZY", "itemLong"},
                */
        };

        for (String[] td  : testData) {
            final Result actual = p.run(td[0]);
            assertEquals(td[1], actual.item.val);
            assertNotNull(actual.fn);
        }


        /*
        actual = p.run("0x1.2 ");
        assertNotEquals("0x1.2", actual.item.val);
        assertNull("0x1.2 ", actual.fn);

        actual = p.run("017 ");
        assertEquals("017", actual.item.val);
        assertNotNull(actual.fn);

        actual = p.run("089 ");
        assertNotEquals("089", actual.item.val);
        assertNull("089 ", actual.fn);
        */
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

    public void Test_rightBracket() {
        WriterQueue q = new WriterQueue();
        Lexable l = Lexable.create("test.clj", ")", q);
    }
}
