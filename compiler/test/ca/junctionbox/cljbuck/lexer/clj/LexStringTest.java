package ca.junctionbox.cljbuck.lexer.clj;

import ca.junctionbox.cljbuck.channel.ReadWriterQueue;
import ca.junctionbox.cljbuck.lexer.Item;
import ca.junctionbox.cljbuck.lexer.Lexable;
import ca.junctionbox.cljbuck.lexer.StateFunc;
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
            final ReadWriterQueue q = new ReadWriterQueue();
            final CljLex cljLex = new CljLex();
            final Lexable lexable = Lexable.create("test.clj", (String) td[0], cljLex, q);

            StateFunc fn = cljLex.string(cljLex.file()).func(lexable);

            Item i = (Item) q.read();

            assertEquals(itemString, i.type);
            assertThat(fn, instanceOf(LexFile.class));

            assertEquals(td[0], i.val);
        }
    }
}