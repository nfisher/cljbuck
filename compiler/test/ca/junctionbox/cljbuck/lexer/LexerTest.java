package ca.junctionbox.cljbuck.lexer;

import org.junit.Test;

import static org.junit.Assert.*;

public class LexerTest {

    @Test
    public void Test_next() {
        final Lexer l = new Lexer("test.clj", "a");

        char c1 = l.next();
        char c2 = l.next();

        assertEquals('a', c1);
        assertEquals(3, c2);
    }

    @Test
    public void Test_ignore() {
        final Lexer l = new Lexer("test.clj", "   ");
        l.acceptRun(" ");
        l.ignore();

        assertEquals(2, l.pos);
    }

    @Test
    public void Test_accept() {
        final Lexer l = new Lexer("test.clj", " ) ");
        l.accept(" ");
        l.accept(")");
        l.accept(" ");
        assertEquals(3, l.pos);
    }
}