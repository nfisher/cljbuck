package ca.junctionbox.cljbuck.lexer;

import org.jcsp.lang.*;
import org.junit.Test;

import java.util.ArrayList;

import static ca.junctionbox.cljbuck.lexer.Funcs.*;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.*;

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
            final One2OneChannel<Object> chan = Channel.one2one();
            final Lexable l = Lexable.create("comment.clj", s, chan.out());
            final Result[] actual = {new Result()};

            CSProcess receiver = () -> {
                final Item item = (Item) chan.in().read();
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
    public void Test_lexText() {
        Result actual;
        Runner p = partial(lexFile);

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
        One2OneChannel<Object> chan = Channel.one2one();
        final Lexable l = Lexable.create("comment.clj",
                "(defn hello [filename] (prn \"Hola \" filename))", chan.out());

        DrainTask task = new DrainTask(chan.in());

        new Parallel(new CSProcess[]{
                (CSProcess) l,
                task,
        }).run();

        String tokens = task.items.stream()
                .filter(item -> item.type != ItemType.itemEOF)
                .map(item -> item.val)
                .reduce((a,b) -> a + " " + b)
                .get();

        assertEquals(task.items.size(), 13);
        assertEquals("( defn hello [ filename ] ( prn \"Hola \" filename ) )", tokens);
    }


    @Test(timeout=1000L)
    public void Test_Lexer_lex_multiple_lines() {
        One2OneChannel<Object> chan = Channel.one2one();
        final Lexable l = Lexable.create("comment.clj",
                "(ns my.core)\n\n\n(defn hello [filename]\n (prn \"Hola \" filename))", chan.out());

        DrainTask task = new DrainTask(chan.in());

        new Parallel(new CSProcess[]{
                (CSProcess) l,
                task,
        }).run();

        String tokens = task.items.stream()
                .filter(item -> item.type != ItemType.itemEOF)
                .map(item -> item.val)
                .reduce((a,b) -> a + " " + b)
                .get();

        assertEquals(task.items.size(), 17);
        assertEquals("( ns my.core ) ( defn hello [ filename ] ( prn \"Hola \" filename ) )", tokens);
    }
}

class DrainTask implements CSProcess {
    public final ArrayList<Item> items = new ArrayList<>();
    private final ChannelInput<Object> in;

    DrainTask(ChannelInput<Object> in) {
        this.in = in;
    }

    @Override
    public void run() {
        for (;;) {
            final Item item = (Item) in.read();
            items.add(item);
            if (item == null || item.type == ItemType.itemEOF) {
                break;
            }
        }
    }
}