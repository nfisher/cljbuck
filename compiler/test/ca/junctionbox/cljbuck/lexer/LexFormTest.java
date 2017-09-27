package ca.junctionbox.cljbuck.lexer;

import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.ItemType.*;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

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
            final WriterQueue q = new WriterQueue();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], q);

            StateFunc fn = new CljLex().form(null).func(lexable);

            Item item = q.read();
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
            final WriterQueue q = new WriterQueue();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], q);

            StateFunc fn = new CljLex().form(null).func(lexable);

            Item item = q.read();

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
            final WriterQueue q = new WriterQueue();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], q);

            StateFunc fn = new CljLex().form(null).func(lexable);

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
            final WriterQueue q = new WriterQueue();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], q);

            StateFunc fn = new CljLex().form(null).func(lexable);

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
            final WriterQueue q = new WriterQueue();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], q);

            StateFunc fn = new CljLex().form(null).func(lexable);

            Item item = q.read();
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
            final WriterQueue q = new WriterQueue();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], q);

            StateFunc fn = new CljLex().form(null).func(lexable);

            assertThat(fn, instanceOf(LexNumeric.class));
            assertEquals(0, q.size());
        }
    }
}