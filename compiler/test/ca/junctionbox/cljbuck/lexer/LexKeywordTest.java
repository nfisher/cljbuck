package ca.junctionbox.cljbuck.lexer;

import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.ItemType.itemError;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemKeyword;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class LexKeywordTest {

    @Test
    public void Test_simple_keyword() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", "keyword ", q);
        final CljLex cljLex = new CljLex();
        final StateFunc fn = cljLex.keyword(cljLex.file()).func(lexable);

        Item i = q.read();
        assertThat(fn, instanceOf(LexFile.class));
        assertEquals(itemKeyword, i.type);
        assertEquals("keyword", i.val);
    }

    @Test
    public void Test_ns_keyword() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", "key/word ", q);
        final CljLex cljLex = new CljLex();
        final StateFunc fn = cljLex.keyword(cljLex.file()).func(lexable);

        Item i = q.read();
        assertThat(fn, instanceOf(LexFile.class));
        assertEquals(itemKeyword, i.type);
        assertEquals("key/word", i.val);
    }

    @Test
    public void Test_too_many_slashes() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", "key//word ", q);
        final CljLex cljLex = new CljLex();
        final StateFunc fn = cljLex.keyword(cljLex.file()).func(lexable);

        Item i = q.read();
        assertNull(fn);
        assertEquals(itemError, i.type);
    }
}