package ca.junctionbox.cljbuck.lexer;

import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.ItemType.itemString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class LexStringTest {

    @Test
    public void Test_string() {
        Object[][] table = {
                {"stuff\"", "basic string"},
                {"stuff\\\"\"", "escaped double quote"},
                {"stuff\\\\\\\"\"", "escaped slash and double quote"},
        };

        for (Object[] td : table) {
            final WriterQueue q = new WriterQueue();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], q);
            final CljLex cljLex = new CljLex();

            StateFunc fn = cljLex.string(cljLex.file()).func(lexable);

            Item i = q.read();

            assertEquals(itemString, i.type);
            assertThat(fn, instanceOf(LexFile.class));

            assertEquals(td[0], i.val);
        }
    }
}