package ca.junctionbox.cljbuck.build;

import ca.junctionbox.cljbuck.channel.ReadWriterQueue;
import ca.junctionbox.cljbuck.channel.Reader;
import ca.junctionbox.cljbuck.channel.Writer;
import ca.junctionbox.cljbuck.lexer.Item;
import ca.junctionbox.cljbuck.lexer.ItemType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static ca.junctionbox.cljbuck.build.rules.Type.*;
import static ca.junctionbox.cljbuck.build.Rules.jar;
import static ca.junctionbox.cljbuck.lexer.ItemType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

class RuleEmitterTask implements Callable<Integer> {
    private final Reader in;
    private final Writer out;

    public RuleEmitterTask(final Reader in, final Writer out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public Integer call() throws Exception {
        Rules currentRule = null;
        String ruleKey = null; // e.g. :name

        for (;;) {
            final Item token = (Item)in.read();

            if (itemEOF == token.type) {
                break;
            } else if (itemLeftParen == token.type) {
                // ignore it for now...
            } else if (itemRightParen == token.type) {
                out.write(currentRule);
                currentRule = null;
                ruleKey = null; // e.g. :name
            } else if (itemSymbol == token.type) {
                switch (token.val) {
                    case "jar":
                        currentRule = jar();
                        break;
                }
            } else if (itemKeyword == token.type) {
                ruleKey = token.val;
            } else if (itemString == token.type) {
                if (ruleKey.equals(":name")) {
                    currentRule = currentRule.name(token.val);
                } else if (ruleKey.equals(":jar")) {
                    currentRule = currentRule.binaryJar(token.val);
                } else if (ruleKey.equals(":deps")) {
                    currentRule = currentRule.appendDep(token.val);
                } else {
                    return -1;
                }
            }
        }
        return 0;
    }
}

public class RuleEmitterTest {
    @Test(timeout=100L)
    public void Test_emit_jar() throws Exception {
        // being lazy with the position here.
        final Item[] items = {
                new Item(itemLeftParen, 0, "(", 0, "CLJ"),
                new Item(itemSymbol, 1, "jar", 0, "CLJ"),
                new Item(itemKeyword, 5, ":name", 0, "CLJ"),
                new Item(itemString, 11, "clojure1.9", 0, "CLJ"),
                new Item(itemKeyword, 5, ":jar", 0, "CLJ"),
                new Item(itemString, 11, "clojure1.9.0-beta1.jar", 0, "CLJ"),
                new Item(itemKeyword, 5, ":deps", 0, "CLJ"),
                new Item(itemLeftBracket, 5, "[", 0, "CLJ"),
                new Item(itemString, 11, ":core.specs.alpha", 0, "CLJ"),
                new Item(itemString, 11, ":spec.alpha", 0, "CLJ"),
                new Item(itemRightBracket, 5, "]", 0, "CLJ"),
                new Item(itemRightParen, 0, ")", 0, "CLJ"),
                new Item(itemEOF, 0, "", 0, "CLJ"),
        };
        final ReadWriterQueue in = new ReadWriterQueue();
        final ReadWriterQueue out = new ReadWriterQueue();

        for (Item i : items) {
            in.write(i);
        }

        final int rc = new RuleEmitterTask(in, out).call();

        assertThat("return code should be 0", rc, is(not(-1)));

        final Rules ruleBuilder = (Rules)out.read();

        assertThat("incorrect type", ruleBuilder.type, is(Jar));
        assertThat("incorrect name", ruleBuilder.name, is("clojure1.9"));
        assertThat("incorrect jar", ruleBuilder.binaryJar, is("clojure1.9.0-beta1.jar"));
        assertThat("incorrect num of deps", ruleBuilder.deps.size(), is(2));
    }

    /*
    @Test
    public void Test_emit_interleaved_tokens() throws Exception {
        // being lazy with the position here.
        final Item[] items = {
                new Item(itemLeftParen, 0, "(", 0, "lib/CLJ"),
                new Item(itemSymbol, 1, "jar", 0, "lib/CLJ"),
                new Item(itemLeftParen, 0, "(", 0, "CLJ"),
                new Item(itemKeyword, 5, ":name", 0, "lib/CLJ"),
                new Item(itemString, 11, "clojure1.9", 0, "lib/CLJ"),
                new Item(itemSymbol, 1, "jar", 0, "CLJ"),
                new Item(itemKeyword, 5, ":name", 0, "CLJ"),
                new Item(itemString, 11, "clojure1.9", 0, "CLJ"),
                new Item(itemKeyword, 5, ":jar", 0, "lib/CLJ"),
                new Item(itemString, 11, "clojure1.9.0-beta1.jar", 0, "lib/CLJ"),
                new Item(itemKeyword, 5, ":deps", 0, "lib/CLJ"),
                new Item(itemKeyword, 5, ":jar", 0, "CLJ"),
                new Item(itemString, 11, "clojure1.9.0-beta1.jar", 0, "CLJ"),
                new Item(itemKeyword, 5, ":deps", 0, "CLJ"),
                new Item(itemLeftBracket, 5, "[", 0, "CLJ"),
                new Item(itemLeftBracket, 5, "[", 0, "lib/CLJ"),
                new Item(itemString, 11, ":core.specs.alpha", 0, "lib/CLJ"),
                new Item(itemString, 11, ":spec.alpha", 0, "lib/CLJ"),
                new Item(itemString, 11, ":core.specs.alpha", 0, "CLJ"),
                new Item(itemString, 11, ":spec.alpha", 0, "CLJ"),
                new Item(itemRightBracket, 5, "]", 0, "CLJ"),
                new Item(itemRightParen, 0, ")", 0, "CLJ"),
                new Item(itemEOF, 0, "", 0, "CLJ"),
                new Item(itemRightBracket, 5, "]", 0, "lib/CLJ"),
                new Item(itemRightParen, 0, ")", 0, "lib/CLJ"),
                new Item(itemEOF, 0, "", 0, "lib/CLJ"),
        };
        final ReadWriterQueue in = new ReadWriterQueue();
        final ReadWriterQueue out = new ReadWriterQueue();

        for (Item i : items) {
            in.write(i);
        }

        final int rc = new RuleEmitterTask(in, out).call();

        assertThat("return code should be 0", rc, is(not(-1)));

        final Rules ruleBuilder = (Rules)out.read();

        assertThat("incorrect type", ruleBuilder.type, is(Jar));
        assertThat("incorrect name", ruleBuilder.name, is("clojure1.9"));
        assertThat("incorrect jar", ruleBuilder.binaryJar, is("clojure1.9.0-beta1.jar"));
        assertThat("incorrect num of deps", ruleBuilder.deps.size(), is(2));
    }
    */
}
