package ca.junctionbox.cljbuck.build;

import ca.junctionbox.cljbuck.channel.ReadWriterQueue;
import ca.junctionbox.cljbuck.lexer.Item;
import ca.junctionbox.cljbuck.lexer.ItemType;
import ca.junctionbox.cljbuck.lexer.Lexable;
import ca.junctionbox.cljbuck.lexer.Lexeme;
import ca.junctionbox.cljbuck.lexer.StateFunc;
import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.CharLexer.EOF;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;



public class LexFileTest {
    private Item getItem(final String contents, final Character ch) {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexeme lexeme = new BuildFile();
        final Lexable lexable = Lexable.create("CLJ", contents, lexeme, q);
        if (null != ch) {
            lexable.push(ch);
        }
        final StateFunc fn = new LexFile().func(lexable);
        return (Item) q.read();
    }

    @Test(timeout=100)
    public void Test_invalid() {
        final Item item = getItem("`", null);

        assertThat(item.type, is(ItemType.itemError));
    }

    @Test(timeout=100)
    public void Test_eof() {
        final Item item = getItem("   ", null);

        assertThat(item.type, is(ItemType.itemEOF));
    }

    @Test(timeout=100)
    public void Test_comment() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexeme lexeme = new BuildFile();
        final Lexable lexable = Lexable.create("CLJ", ";   \n", lexeme, q);
        new LexFile().func(lexable);

        assertThat(lexable.next(), is(EOF));
    }

    @Test(timeout=100)
    public void Test_left_parens() {
        final Item item = getItem("    (", null);

        assertThat(item.type, is(ItemType.itemLeftParen));
    }

    @Test(timeout=100)
    public void Test_right_parens() {
        final Item item = getItem(" )", '(');

        assertThat(item.type, is(ItemType.itemRightParen));
    }

    @Test(timeout=100)
    public void Test_left_bracket() {
        final Item item = getItem("[", null);
        assertThat(item.type, is(ItemType.itemLeftBracket));
    }

    @Test(timeout=100)
    public void Test_right_bracket() {
        final Item item = getItem("]", '[');
        assertThat(item.type, is(ItemType.itemRightBracket));
    }

    @Test(timeout=100)
    public void Test_string() {
        final Item item = getItem("\"ns.core\"", null);
        assertThat(item.type, is(ItemType.itemString));
        assertThat(item.val, is("ns.core"));
    }

    @Test(timeout=100)
    public void Test_keyword() {
        final Item item = getItem(":name", null);
        assertThat(item.type, is(ItemType.itemKeyword));
        assertThat(item.val, is(":name"));
    }

    @Test(timeout=100)
    public void Test_symbol() {
        final Item item = getItem("clj-lib", null);
        assertThat(item.type, is(ItemType.itemSymbol));
        assertThat(item.val, is("clj-lib"));
    }
}