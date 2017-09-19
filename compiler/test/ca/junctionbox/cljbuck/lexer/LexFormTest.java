package ca.junctionbox.cljbuck.lexer;

import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.Funcs.lexForm;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftBrace;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftBracket;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftParen;
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
}