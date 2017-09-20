package ca.junctionbox.cljbuck.lexer;

import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.Funcs.lexFile;
import static ca.junctionbox.cljbuck.lexer.Funcs.lexString;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemString;
import static org.junit.Assert.assertEquals;

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

            StateFunc fn = lexString.func(lexable);

            Item i = q.read();
            assertEquals(lexFile, fn);
            assertEquals(itemString, i.type);
            assertEquals(td[0], i.val);
        }
    }
}