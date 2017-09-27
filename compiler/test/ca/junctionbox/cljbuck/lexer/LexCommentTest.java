package ca.junctionbox.cljbuck.lexer;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class LexCommentTest {
    @Test
    public void Test_newline() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", " stuff it\n", q);
        final CljLex cljLex = new CljLex();
        final StateFunc fn = cljLex.comment(cljLex.file()).func(lexable);

        assertThat(fn, instanceOf(LexFile.class));
        assertEquals(1, q.size());
    }

    @Test
    public void Test_eof() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", " stuff it", q);
        final CljLex cljLex = new CljLex();
        final StateFunc fn = cljLex.comment(cljLex.file()).func(lexable);

        assertThat(fn, instanceOf(LexFile.class));
        assertEquals(1, q.size());
    }
}