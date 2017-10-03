package ca.junctionbox.cljbuck.build;

import ca.junctionbox.cljbuck.channel.ReadWriterQueue;
import ca.junctionbox.cljbuck.lexer.Item;
import ca.junctionbox.cljbuck.lexer.Lexable;
import ca.junctionbox.cljbuck.lexer.Lexeme;
import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.ItemType.itemKeyword;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftBracket;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftParen;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemRightBracket;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemRightParen;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemString;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemSymbol;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class BuildFileTest {
    public static final String libClj = "(jar :name \"clojure1.9\"\n" +
            "     :jar \"clojure-1.9.0-beta1.jar\"\n" +
            "     :deps [\":core.specs.alpha\", \":spec.alpha\"]\n" +
            "     :visibility [\"PUBLIC\"])\n" +
            "\n" +
            "(jar :name \"spec.alpha\"\n" +
            "     :jar \"spec.alpha-0.1.123.jar\")\n" +
            "\n" +
            "(jar :name \"core.specs.alpha\"\n" +
            "     :jar \"core.specs.alpha-0.1.24.jar\")";

    public static final String jbxClj = "(clj-lib :name \"lib\"\n" +
            "         :ns \"jbx.core\"\n" +
            "         :srcs [\"src/clj/**/*.clj\", \"src/cljc/**/*.cljc\"]\n" +
            "         :deps [\"//lib:clojure1.9\"])\n" +
            "\n" +
            "(clj-binary :name \"main\"\n" +
            "            :main \"jbx.core\"\n" +
            "            :deps [\":lib\"])\n" +
            "\n" +
            "(clj-test :name \"test\"\n" +
            "          :srcs [\"test/clj/**/*.clj\"]\n" +
            "          :deps [\"//lib:clojure1.9\", \":lib\"])";

    @Test
    public void Test_libClj() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexeme lexeme = new BuildFile();
        final Lexable lexable = Lexable.create("CLJ", libClj, lexeme, q);
        lexable.run();

        final int leftParens = (int) q.stream().filter(item -> ((Item)item).type == itemLeftParen).count();
        final int rightParens = (int) q.stream().filter(item -> ((Item)item).type == itemRightParen).count();
        final int leftBracket = (int) q.stream().filter(item -> ((Item)item).type == itemLeftBracket).count();
        final int rightBracket = (int) q.stream().filter(item -> ((Item)item).type == itemRightBracket).count();
        final int keywords = (int) q.stream().filter(item -> ((Item)item).type == itemKeyword).count();
        final int strings =  (int) q.stream().filter(item -> ((Item)item).type == itemString).count();
        final int symbols =  (int) q.stream().filter(item -> ((Item)item).type == itemSymbol).count();

        assertThat("unbalanced parens", leftParens, is(rightParens));
        assertThat("incorrect number of parens", leftParens, is(3));
        assertThat("unbalanced brackets", leftBracket, is(rightBracket));
        assertThat("incorrect number of brackets", leftBracket, is(2));
        assertThat("incorrect number of keywords", keywords, is(8));
        assertThat("incorrect number of strings", strings, is(9));
        assertThat("incorrect number of symbols", symbols, is(3));
        assertThat(q.size(), is(31));
    }

    @Test
    public void Test_jbxClj() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexeme lexeme = new BuildFile();
        final Lexable lexable = Lexable.create("CLJ", jbxClj, lexeme, q);
        lexable.run();

        final int leftParens = (int) q.stream().filter(item -> ((Item)item).type == itemLeftParen).count();
        final int rightParens = (int) q.stream().filter(item -> ((Item)item).type == itemRightParen).count();
        final int leftBracket = (int) q.stream().filter(item -> ((Item)item).type == itemLeftBracket).count();
        final int rightBracket = (int) q.stream().filter(item -> ((Item)item).type == itemRightBracket).count();
        final int keywords = (int) q.stream().filter(item -> ((Item)item).type == itemKeyword).count();
        final int strings =  (int) q.stream().filter(item -> ((Item)item).type == itemString).count();
        final int symbols =  (int) q.stream().filter(item -> ((Item)item).type == itemSymbol).count();

        assertThat("unbalanced parens", leftParens, is(rightParens));
        assertThat("incorrect number of parens", leftParens, is(3));
        assertThat("unbalanced brackets", leftBracket, is(rightBracket));
        assertThat("incorrect number of brackets", leftBracket, is(5));
        assertThat("incorrect number of keywords", keywords, is(10));
        assertThat("incorrect number of strings", strings, is(12));
        assertThat("incorrect number of symbols", symbols, is(3));
    }
}