package ca.junctionbox.cljbuck.lexer;

import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.Funcs.lexNumeric;
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


        for (String td  : testData) {
            WriterQueue q = new WriterQueue();
            Lexable lexable = Lexable.create("test.clj", td, q);

            StateFunc fn = lexNumeric.func(lexable);

            assertEquals(td,"NumberFormatException Invalid number", q.read().val);
            assertNull(td, fn);
        }
    }

    @Test(timeout = 100L)
    public void Test_valid_numbers() {
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
            WriterQueue q = new WriterQueue();
            Lexable lexable = Lexable.create("test.clj", td[0], q);
            StateFunc fn = lexNumeric.func(lexable);
            assertEquals(td[1], q.read().val);
            assertNotNull(fn);
        }
    }
}