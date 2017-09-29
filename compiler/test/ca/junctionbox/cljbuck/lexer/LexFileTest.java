package ca.junctionbox.cljbuck.lexer;

import ca.junctionbox.cljbuck.channel.ReadWriterQueue;
import ca.junctionbox.cljbuck.lexer.clj.CljLex;
import ca.junctionbox.cljbuck.lexer.clj.LexComment;
import ca.junctionbox.cljbuck.lexer.clj.LexForm;
import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.ItemType.itemError;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftParen;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LexFileTest {
    @Test
    public void Test_left_list_form() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexable lexable = Lexable.create("test.clj", "(", q);
        final StateFunc fn = new CljLex().file().func(lexable);

        Item item = (Item) q.read();
        assertThat(fn, instanceOf(LexForm.class));
        assertThat(item.type, is(itemLeftParen));
    }

    @Test
    public void Test_unmatched_right_list_form() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexable lexable = Lexable.create("test.clj", ")", q);
        assertTrue("Not Empty", lexable.empty());
        final StateFunc fn = new CljLex().file().func(lexable);

        Item item = (Item) q.read();
        assertThat(fn, is(nullValue()));
        assertThat(item.type, is(itemError));
    }

    @Test
    public void Test_whitespace_collapse() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexable lexable = Lexable.create("test.clj", " \n\t(", q);
        final StateFunc fn = new CljLex().file().func(lexable);

        Item item = (Item) q.read();
        assertThat(fn, instanceOf(LexForm.class));
        assertThat(item.type, is(itemLeftParen));
    }

    @Test
    public void Test_comment() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexable lexable = Lexable.create("test.clj", " \n\t; ", q);
        final StateFunc fn = new CljLex().file().func(lexable);

        assertThat(fn, instanceOf(LexComment.class));
        assertThat(q.size(), is(0));
    }
}