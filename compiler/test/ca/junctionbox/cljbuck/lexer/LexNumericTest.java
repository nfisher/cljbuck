package ca.junctionbox.cljbuck.lexer;

import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.Funcs.lexFile;
import static ca.junctionbox.cljbuck.lexer.Funcs.lexForm;
import static ca.junctionbox.cljbuck.lexer.Funcs.lexNumeric;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftParen;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LexNumericTest {
    @Test
    public void Test_left_list_form() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", "(", q);
        final StateFunc fn = lexFile.func(lexable);

        assertEquals(lexForm, fn);
        assertEquals(itemLeftParen, q.read().type);
    }

    @Test(timeout = 100L)
    public void Test_lexNumber_with_valid_numbers() {
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

}