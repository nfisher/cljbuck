package ca.junctionbox.cljbuck.lexer;

import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.Funcs.*;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemError;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftParen;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LexFileTest {
    @Test
    public void Test_left_list_form() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", "(", q);
        final StateFunc fn = lexFile.func(lexable);

        assertEquals(lexForm, fn);
        assertEquals(itemLeftParen, q.read().type);
    }

    @Test
    public void Test_unmatched_right_list_form() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", ")", q);
        assertTrue("Not Empty", lexable.empty());
        final StateFunc fn = lexFile.func(lexable);

        assertNull(fn);
        assertEquals(itemError, q.read().type);
    }

    @Test
    public void Test_whitespace_collapse() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", " \n\t(", q);
        final StateFunc fn = lexFile.func(lexable);

        assertEquals(lexForm, fn);
        assertEquals(itemLeftParen, q.read().type);
    }

    @Test
    public void Test_comment() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", " \n\t; ", q);
        final StateFunc fn = lexFile.func(lexable);

        assertEquals(lexComment, fn);
        assertEquals(0, q.size());
    }
}