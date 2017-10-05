package ca.junctionbox.cljbuck.lexer.clj;

import ca.junctionbox.cljbuck.channel.ReadWriterQueue;
import ca.junctionbox.cljbuck.lexer.Item;
import ca.junctionbox.cljbuck.lexer.Lexable;
import ca.junctionbox.cljbuck.lexer.StateFunc;
import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.ItemType.itemBacktick;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemDeref;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemDispatch;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemError;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftBrace;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftBracket;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftParen;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLiteral;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemMeta;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemQuote;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemUnquote;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class LexFormTest {
    public String sn(final Class<?> c) {
        return c.getSimpleName();
    }

    @Test
    public void Test_collections_start() {
        Object[][] table = {
                {"(", itemLeftParen,},
                {"[", itemLeftBracket,},
                {"{", itemLeftBrace,},
        };

        for (Object[] td : table) {
            final CljLex cljLex = new CljLex();
            final ReadWriterQueue q = new ReadWriterQueue();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], cljLex, q);

            StateFunc fn = cljLex.form(null).func(lexable);

            Item item = (Item) q.read();
            assertThat(fn, instanceOf(LexForm.class));
            assertThat(item.type, is(td[1]));
        }
    }

    @Test
    public void Test_collections_unbalanced_bracket() {
        Object[][] table = {
                {")"},
                {"]"},
                {"}"},
        };

        for (Object[] td : table) {
            final ReadWriterQueue q = new ReadWriterQueue();
            final CljLex cljLex = new CljLex();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], cljLex, q);

            StateFunc fn = cljLex.form(null).func(lexable);

            Item item = (Item) q.read();

            assertThat(fn, is(nullValue()));
            assertThat(item.type, is(itemError));
        }
    }

    @Test
    public void Test_values() {
        Object[][] table = {
                {";", sn(LexComment.class)},
                {":", sn(LexKeyword.class)},
                {"\"", sn(LexString.class)},
        };

        for (Object[] td : table) {
            final ReadWriterQueue q = new ReadWriterQueue();
            final CljLex cljLex = new CljLex();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], cljLex, q);

            StateFunc fn = cljLex.form(null).func(lexable);

            assertThat(q.size(), is(0));
            assertThat(sn(fn.getClass()), is(td[1]));
        }
    }

    @Test
    public void Test_symbols() {
        Object[][] table = {
                {"->>"},
                {"defn"},
                {"*ns*"},
                {".setName"},
                {"+setName"},
        };

        for (Object[] td : table) {
            final ReadWriterQueue q = new ReadWriterQueue();
            final CljLex cljLex = new CljLex();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], cljLex, q);

            StateFunc fn = cljLex.form(null).func(lexable);

            assertThat(fn, instanceOf(LexSymbol.class));
            assertThat(q.size(), is(0));
        }
    }

    @Test
    public void Test_macro_characters() {
        Object[][] table = {
                {"'", sn(LexForm.class), itemQuote},
                {"\\", sn(LexSymbol.class), itemLiteral},
                {"@", sn(LexSymbol.class), itemDeref},
                {"^", sn(LexForm.class), itemMeta},
                {"#", sn(LexForm.class), itemDispatch},
                {"`", sn(LexForm.class), itemBacktick},
                {"~", sn(LexForm.class), itemUnquote},
        };

        for (Object[] td : table) {
            final ReadWriterQueue q = new ReadWriterQueue();
            final CljLex cljLex = new CljLex();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], cljLex, q);

            StateFunc fn = cljLex.form(null).func(lexable);

            Item item = (Item) q.read();
            assertThat(sn(fn.getClass()), is(td[1]));
            assertThat(item.type, is(td[2]));
        }
    }

    @Test
    public void Test_numeric() {
        Object[][] table = {
                {"0",},
                {"1",},
                {"2",},
                {"3",},
                {"4",},
                {"5",},
                {"6",},
                {"7",},
                {"8",},
                {"9",},
        };

        for (Object[] td : table) {
            final ReadWriterQueue q = new ReadWriterQueue();
            final CljLex cljLex = new CljLex();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], cljLex, q);

            StateFunc fn = cljLex.form(null).func(lexable);

            assertThat(fn, instanceOf(LexNumeric.class));
            assertEquals(0, q.size());
        }
    }
}