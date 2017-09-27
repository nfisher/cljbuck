package ca.junctionbox.cljbuck.lexer;

import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.ItemType.itemError;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftParen;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LexFileTest {
    @Test
    public void Test_left_list_form() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", "(", q);
        final StateFunc fn = new CljLex().file().func(lexable);

        assertThat(fn, instanceOf(LexForm.class));
        assertThat(q.read().type, is(itemLeftParen));
    }

    @Test
    public void Test_unmatched_right_list_form() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", ")", q);
        assertTrue("Not Empty", lexable.empty());
        final StateFunc fn = new CljLex().file().func(lexable);

        assertThat(fn, is(nullValue()));
        assertThat(q.read().type, is(itemError));
    }

    @Test
    public void Test_whitespace_collapse() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", " \n\t(", q);
        final StateFunc fn = new CljLex().file().func(lexable);

        assertThat(fn, instanceOf(LexForm.class));
        assertThat(q.read().type, is(itemLeftParen));
    }

    @Test
    public void Test_comment() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", " \n\t; ", q);
        final StateFunc fn = new CljLex().file().func(lexable);

        assertThat(fn, instanceOf(LexComment.class));
        assertThat(q.size(), is(0));
    }
}