package ca.junctionbox.cljbuck.lexer;

import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.Funcs.*;
import static ca.junctionbox.cljbuck.lexer.ItemType.*;
import static org.junit.Assert.*;

public class LexFormTest {

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

            StateFunc fn = lexForm.func(lexable);

            Item item = q.read();
            assertEquals(lexForm, fn);
            assertEquals(td[1], item.type);
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

            StateFunc fn = lexForm.func(lexable);

            Item item = q.read();
            assertNull(fn);
            assertEquals(itemError, item.type);
        }
    }

    @Test
    public void Test_values() {
        Object[][] table = {
                {";", lexComment},
                {":", lexKeyword},
                {"\"", lexString},
        };

        for (Object[] td : table) {
            final WriterQueue q = new WriterQueue();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], q);

            StateFunc fn = lexForm.func(lexable);

            assertEquals(0, q.size());
            assertEquals(td[1], fn);
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

            StateFunc fn = lexForm.func(lexable);

            assertEquals(lexSymbol, fn);
            assertEquals(0, q.size());
        }
    }

    @Test
    public void Test_macro_characters() {
        Object[][] table = {
                {"'", lexForm, itemQuote},
                {"\\", lexSymbol, itemLiteral},
                {"@", lexSymbol, itemDeref},
                {"^", lexForm, itemMeta},
                {"#", lexForm, itemDispatch},
                {"`", lexForm, itemBacktick},
                {"~", lexForm, itemUnquote},
        };

        for (Object[] td : table) {
            final WriterQueue q = new WriterQueue();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], q);

            StateFunc fn = lexForm.func(lexable);

            Item item = q.read();
            assertEquals(td[1], fn);
            assertEquals(td[2], item.type);
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

            StateFunc fn = lexForm.func(lexable);

            assertEquals(lexNumeric, fn);
            assertEquals(0, q.size());
        }
    }
}