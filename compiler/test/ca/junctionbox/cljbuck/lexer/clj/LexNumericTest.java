package ca.junctionbox.cljbuck.lexer.clj;

import ca.junctionbox.cljbuck.channel.ReadWriterQueue;
import ca.junctionbox.cljbuck.lexer.Item;
import ca.junctionbox.cljbuck.lexer.Lexable;
import ca.junctionbox.cljbuck.lexer.StateFunc;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LexNumericTest {

    @Test
    public void Test_invalid_numbers() {
        String[] testData = {
                "36T ",
                "0x1.2 ",
                "098 ",
        };


        for (String td : testData) {
            final ReadWriterQueue q = new ReadWriterQueue();
            final CljLex cljLex = new CljLex();
            final Lexable lexable = Lexable.create("test.clj", td, cljLex, q);

            StateFunc fn = cljLex.numeric(null).func(lexable);

            Item item = (Item) q.read();
            assertEquals(td, "NumberFormatException Invalid number", item.val);
            assertNull(td, fn);
        }
    }

    @Test(timeout = 100L)
    public void Test_valid_numbers() {
        final String[][] testData = {
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

        for (String[] td : testData) {
            final ReadWriterQueue q = new ReadWriterQueue();
            final CljLex cljLex = new CljLex();
            final Lexable lexable = Lexable.create("test.clj", td[0], cljLex, q);
            final StateFunc fn = cljLex.numeric(cljLex.file()).func(lexable);
            Item item = (Item) q.read();
            assertEquals(td[1], item.val);
            assertNotNull(fn);
        }
    }
}