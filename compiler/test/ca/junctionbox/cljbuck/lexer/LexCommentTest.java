package ca.junctionbox.cljbuck.lexer;

import ca.junctionbox.cljbuck.channel.ReadWriterQueue;
import ca.junctionbox.cljbuck.lexer.clj.LexFile;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class LexCommentTest {
    @Test
    public void Test_newline() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexable lexable = Lexable.create("test.clj", " stuff it\n", new CljLex(), q);
        final CljLex cljLex = new CljLex();
        final StateFunc fn = cljLex.comment(cljLex.file()).func(lexable);

        assertThat(fn, instanceOf(LexFile.class));
        assertEquals(1, q.size());
    }

    @Test
    public void Test_eof() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexable lexable = Lexable.create("test.clj", " stuff it", new CljLex(), q);
        final CljLex cljLex = new CljLex();
        final StateFunc fn = cljLex.comment(cljLex.file()).func(lexable);

        assertThat(fn, instanceOf(LexFile.class));
        assertEquals(1, q.size());
    }
}