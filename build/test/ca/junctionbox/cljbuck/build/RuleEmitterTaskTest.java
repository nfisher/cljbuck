package ca.junctionbox.cljbuck.build;

import ca.junctionbox.cljbuck.build.json.Tracer;
import ca.junctionbox.cljbuck.channel.ReadWriterQueue;
import ca.junctionbox.cljbuck.channel.Writer;
import ca.junctionbox.cljbuck.lexer.Item;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static ca.junctionbox.cljbuck.build.rules.Type.CljBinary;
import static ca.junctionbox.cljbuck.build.rules.Type.CljLib;
import static ca.junctionbox.cljbuck.build.rules.Type.CljTest;
import static ca.junctionbox.cljbuck.build.rules.Type.Jar;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemEOF;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemKeyword;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftBracket;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftParen;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemRightBracket;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemRightParen;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemShutdown;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemString;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemSymbol;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class RuleEmitterTaskTest {
    // (clj-lib :name "lib" :ns "jbx.core" :srcs ["src/clj/**/*.clj", "src/cljc/**/*.cljc"] :deps ["//lib:clojure1.9"])
    @Test(timeout=100L)
    public void Test_emit_clj_lib() throws Exception {
        final Item[] items = {
                new Item(itemLeftParen, 0, "(", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemSymbol, 1, "clj-lib", 0, "/home/nfisher/prj/CLJ"),

                // :name lib
                new Item(itemKeyword, 5, ":name", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, "lib", 0, "/home/nfisher/prj/CLJ"),

                // ns jbx.core
                new Item(itemKeyword, 5, ":ns", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, "jbx.core", 0, "/home/nfisher/prj/CLJ"),

                // :srcs [test/clj/**/*.clj, test/cljc/**/*.cljc]
                new Item(itemKeyword, 5, ":srcs", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemLeftBracket, 5, "[", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, "src/clj/**/*.clj", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, "src/cljc/**/*.cljc", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemRightBracket, 5, "]", 0, "/home/nfisher/prj/CLJ"),

                // :deps [//lib:clojure1.9]
                new Item(itemKeyword, 5, ":deps", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemLeftBracket, 5, "[", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, "//lib:clojure1.9", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemRightBracket, 5, "]", 0, "/home/nfisher/prj/CLJ"),

                new Item(itemRightParen, 0, ")", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemEOF, 0, "", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemShutdown,0, "", 0, "/home/nfisher/prj/CLJ")
        };

        final ReadWriterQueue out = new ReadWriterQueue();
        final int rc = callEmitter(items, out);

        assertThat("return code should be 0", rc, is(not(-1)));

        final Rules ruleBuilder = (Rules)out.read();

        assertThat("incorrect type", ruleBuilder.type, is(CljLib));
        assertThat("incorrect name", ruleBuilder.name, is("//:lib"));
        assertThat("incorrect name", ruleBuilder.ns, is("jbx.core"));
        assertThat("incorrect srcs[0]", ruleBuilder.srcs.get(0), is("/home/nfisher/prj/src/clj/**/*.clj"));
        assertThat("incorrect srcs[1]", ruleBuilder.srcs.get(1), is("/home/nfisher/prj/src/cljc/**/*.cljc"));
        assertThat("incorrect num of deps", ruleBuilder.deps.size(), is(1));
        assertThat("incorrect visibility", ruleBuilder.visibility.size(), is(0));
    }

    // (clj-binary :name "main" :main "jbx.core" :deps [":lib"])
    @Test(timeout=100L)
    public void Test_emit_clj_binary() throws Exception {
        final Item[] items = {
                new Item(itemLeftParen, 0, "(", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemSymbol, 1, "clj-binary", 0, "/home/nfisher/prj/CLJ"),

                // :name test
                new Item(itemKeyword, 5, ":name", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, "main", 0, "/home/nfisher/prj/CLJ"),

                // :srcs [test/clj/**/*.clj]
                new Item(itemKeyword, 5, ":main", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, "jbx.core", 0, "/home/nfisher/prj/CLJ"),

                // :deps [:lib]
                new Item(itemKeyword, 5, ":deps", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemLeftBracket, 5, "[", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, ":lib", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemRightBracket, 5, "]", 0, "/home/nfisher/prj/CLJ"),

                new Item(itemRightParen, 0, ")", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemEOF, 0, "", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemShutdown,0, "", 0, "/home/nfisher/prj/CLJ")
        };
        final ReadWriterQueue out = new ReadWriterQueue();
        final int rc = callEmitter(items, out);

        assertThat("return code should be 0", rc, is(not(-1)));

        final Rules ruleBuilder = (Rules)out.read();

        assertThat("incorrect type", ruleBuilder.type, is(CljBinary));
        assertThat("incorrect name", ruleBuilder.name, is("//:main"));
        assertThat("incorrect name", ruleBuilder.main, is("jbx.core"));
        assertThat("incorrect num of deps", ruleBuilder.deps.size(), is(1));
        assertThat("incorrect visibility", ruleBuilder.visibility.size(), is(0));
    }

    // (clj-test :name "test" :srcs ["test/clj/ ** /*.clj"] :deps ["//lib:clojure1.9", ":lib"])
    @Test(timeout=100L)
    public void Test_emit_clj_test() throws Exception {
        final Item[] items = {
                new Item(itemLeftParen, 0, "(", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemSymbol, 1, "clj-test", 0, "/home/nfisher/prj/CLJ"),

                // :name test
                new Item(itemKeyword, 5, ":name", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, "test", 0, "/home/nfisher/prj/CLJ"),

                // :srcs [test/clj/**/*.clj]
                new Item(itemKeyword, 5, ":srcs", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemLeftBracket, 5, "[", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, "test/clj/**/*.clj", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemRightBracket, 5, "]", 0, "/home/nfisher/prj/CLJ"),

                // :deps [//lib:clojure1.9, :lib]
                new Item(itemKeyword, 5, ":deps", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemLeftBracket, 5, "[", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, "//lib:clojure1.9", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, ":lib", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemRightBracket, 5, "]", 0, "/home/nfisher/prj/CLJ"),

                new Item(itemRightParen, 0, ")", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemEOF, 0, "", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemShutdown,0, "", 0, "/home/nfisher/prj/CLJ")
        };
        final ReadWriterQueue out = new ReadWriterQueue();
        final int rc = callEmitter(items, out);

        assertThat("return code should be 0", rc, is(not(-1)));

        final Rules ruleBuilder = (Rules)out.read();

        assertThat("incorrect type", ruleBuilder.type, is(CljTest));
        assertThat("incorrect name", ruleBuilder.name, is("//:test"));
        assertThat("incorrect num of deps", ruleBuilder.deps.size(), is(2));
        assertThat("unexpected dep[0]", ruleBuilder.deps.get(0), is("//lib:clojure1.9"));
        assertThat("unexpected dep[1]", ruleBuilder.deps.get(1), is("//:lib"));
        assertThat("incorrect num of srcs", ruleBuilder.srcs.size(), is(1));
        assertThat("incorrect visibility", ruleBuilder.visibility.size(), is(0));
    }

    @Test(timeout=100L)
    public void Test_emit_jar() throws Exception {
        final Item[] items = {
                new Item(itemLeftParen, 0, "(", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemSymbol, 1, "jar", 0, "/home/nfisher/prj/CLJ"),

                // :name clojure1.9
                new Item(itemKeyword, 5, ":name", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, "clojure1.9", 0, "/home/nfisher/prj/CLJ"),
                // :jar clojure1.9.0-beta1.jar
                new Item(itemKeyword, 5, ":jar", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, "clojure1.9.0-beta1.jar", 0, "/home/nfisher/prj/CLJ"),

                // :deps [":core.specs.alpha", ":spec.alpha"]
                new Item(itemKeyword, 5, ":deps", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemLeftBracket, 5, "[", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, ":core.specs.alpha", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, ":spec.alpha", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemRightBracket, 5, "]", 0, "/home/nfisher/prj/CLJ"),

                // :visibility ["PUBLIC"]
                new Item(itemKeyword, 5, ":visibility", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemLeftBracket, 5, "[", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, "PUBLIC", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemRightBracket, 5, "]", 0, "/home/nfisher/prj/CLJ"),

                new Item(itemRightParen, 0, ")", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemEOF, 0, "", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemShutdown,0, "", 0, "/home/nfisher/prj/CLJ")
        };
        final ReadWriterQueue out = new ReadWriterQueue();
        final int rc = callEmitter(items, out);

        assertThat("return code should be 0", rc, is(not(-1)));

        final Rules ruleBuilder = (Rules)out.read();

        assertThat("incorrect type", ruleBuilder.type, is(Jar));
        assertThat("incorrect name", ruleBuilder.name, is("//:clojure1.9"));
        assertThat("incorrect jar", ruleBuilder.binaryJar, is("/home/nfisher/prj/clojure1.9.0-beta1.jar"));
        assertThat("incorrect num of deps", ruleBuilder.deps.size(), is(2));
        assertThat("incorrect visibility", ruleBuilder.visibility.get(0), is("PUBLIC"));
    }

    @Test
    public void Test_emit_interleaved_tokens() throws Exception {
        final Item[] items = {
                new Item(itemLeftParen, 0, "(", 0, "/home/nfisher/prj/lib/CLJ"),
                new Item(itemSymbol, 1, "jar", 0, "/home/nfisher/prj/lib/CLJ"),
                new Item(itemLeftParen, 0, "(", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemKeyword, 5, ":name", 0, "/home/nfisher/prj/lib/CLJ"),
                new Item(itemString, 11, "clojure1.8", 0, "/home/nfisher/prj/lib/CLJ"),
                new Item(itemSymbol, 1, "jar", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemKeyword, 5, ":name", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, "clojure1.9", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemKeyword, 5, ":jar", 0, "/home/nfisher/prj/lib/CLJ"),
                new Item(itemKeyword, 5, ":jar", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, "clojure1.8.0.jar", 0, "/home/nfisher/prj/lib/CLJ"),
                new Item(itemString, 11, "clojure1.9.0-beta1.jar", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemKeyword, 5, ":deps", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemLeftBracket, 5, "[", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemString, 11, ":core.specs.alpha", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemRightParen, 0, ")", 0, "/home/nfisher/prj/lib/CLJ"),
                new Item(itemString, 11, ":spec.alpha", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemEOF, 0, "", 0, "/home/nfisher/prj/lib/CLJ"),
                new Item(itemRightBracket, 5, "]", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemRightParen, 0, ")", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemEOF, 0, "", 0, "/home/nfisher/prj/CLJ"),
                new Item(itemShutdown,0, "", 0, "/home/nfisher/prj/CLJ")
        };

        final ReadWriterQueue out = new ReadWriterQueue();

        final int rc = callEmitter(items, out);

        assertThat("return code should be 0", rc, is(not(-1)));
        assertThat("out size", out.size(), is(2));

        final Rules ruleBuilder = (Rules)out.read();

        assertThat("incorrect type", ruleBuilder.type, is(Jar));
        assertThat("incorrect name", ruleBuilder.name, is("//lib:clojure1.8"));
        assertThat("incorrect jar", ruleBuilder.binaryJar, is("/home/nfisher/prj/lib/clojure1.8.0.jar"));
        assertThat("incorrect num of deps", ruleBuilder.deps.size(), is(0));

        final Rules ruleBuilder2 = (Rules)out.read();

        assertThat("incorrect type", ruleBuilder2.type, is(Jar));
        assertThat("incorrect name", ruleBuilder2.name, is("//:clojure1.9"));
        assertThat("incorrect jar", ruleBuilder2.binaryJar, is("/home/nfisher/prj/clojure1.9.0-beta1.jar"));
        assertThat("incorrect num of deps", ruleBuilder2.deps.size(), is(2));
    }

    private int callEmitter(Item[] items, Writer out) throws Exception {
        final ReadWriterQueue in = new ReadWriterQueue();

        for (Item i : items) {
            in.write(i);
        }

        return new RuleEmitterTask(Tracer.create("."), in, out, new Workspace("/home/nfisher/prj"), 1).call();
    }
}
