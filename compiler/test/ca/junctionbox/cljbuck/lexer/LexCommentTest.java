package ca.junctionbox.cljbuck.lexer;

import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.Funcs.lexComment;
import static ca.junctionbox.cljbuck.lexer.Funcs.lexForm;
import static org.junit.Assert.*;

public class LexCommentTest {

    @Test
    public void Test_newline() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", " stuff it\n", q);
        final StateFunc fn = lexComment.func(lexable);

        assertEquals(lexForm, fn);
        assertEquals(1, q.size());
    }

    @Test
    public void Test_eof() {
        final WriterQueue q = new WriterQueue();
        final Lexable lexable = Lexable.create("test.clj", " stuff it", q);
        final StateFunc fn = lexComment.func(lexable);

        assertEquals(lexForm, fn);
        assertEquals(1, q.size());
    }
}