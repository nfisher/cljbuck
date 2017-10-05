package ca.junctionbox.cljbuck.lexer.clj;

import ca.junctionbox.cljbuck.channel.ReadWriterQueue;
import ca.junctionbox.cljbuck.lexer.Item;
import ca.junctionbox.cljbuck.lexer.Lexable;
import ca.junctionbox.cljbuck.lexer.Lexeme;
import ca.junctionbox.cljbuck.lexer.StateFunc;
import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.ItemType.itemError;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftParen;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LexFileTest {
    @Test
    public void Test_left_list_form() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexeme lexeme = new CljLex();
        final Lexable lexable = Lexable.create("test.clj","(",lexeme, q);
        final StateFunc fn = lexeme.file().func(lexable);

        Item item = (Item) q.read();
        assertThat(fn, instanceOf(LexForm.class));
        assertThat(item.type, is(itemLeftParen));
    }

    @Test
    public void Test_unmatched_right_list_form() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexeme lexeme = new CljLex();
        final Lexable lexable = Lexable.create("test.clj", ")", lexeme, q);
        assertTrue("Not Empty", lexable.empty());
        final StateFunc fn = lexeme.file().func(lexable);

        Item item = (Item) q.read();
        assertThat(fn, is(nullValue()));
        assertThat(item.type, is(itemError));
    }

    @Test
    public void Test_whitespace_collapse() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexeme lexeme = new CljLex();
        final Lexable lexable = Lexable.create("test.clj", " \n\t(", lexeme, q);
        final StateFunc fn = lexeme.file().func(lexable);

        Item item = (Item) q.read();
        assertThat(fn, instanceOf(LexForm.class));
        assertThat(item.type, is(itemLeftParen));
    }

    @Test
    public void Test_comment() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexeme lexeme = new CljLex();
        final Lexable lexable = Lexable.create("test.clj", " \n\t; ", lexeme, q);
        final StateFunc fn = lexeme.file().func(lexable);

        assertThat(fn, instanceOf(LexComment.class));
        assertThat(q.size(), is(0));
    }
}