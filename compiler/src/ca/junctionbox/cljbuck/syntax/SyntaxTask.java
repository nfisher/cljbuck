package ca.junctionbox.cljbuck.syntax;

import ca.junctionbox.cljbuck.build.json.Tracer;
import ca.junctionbox.cljbuck.channel.Closer;
import ca.junctionbox.cljbuck.channel.Reader;
import ca.junctionbox.cljbuck.lexer.Item;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import static ca.junctionbox.cljbuck.build.json.Event.finished;
import static ca.junctionbox.cljbuck.build.json.Event.started;

enum SyntaxType {
    CljFile,

    // Collections
    List,   // ()
    Map,    // {}
    Set,    // #{}
    Vector, // []

    Symbol,

    // LITERALS

    Bool,
    Nil,

    // Numbers
    Long,
    BigInt,
    Ratio,
    Double,
    BigDecimal,

    // Stringy
    Str,                // "string"
    Char,               // \a
    Keyword,            // :hello/world

    // MACROS

    Comment,            // ;
    Quote,              // '
    Backslash,          // \
    Deref,              // @
    Metadata,           // ^
    Backquote,          // `
    Unquote,            // ~
    UnquoteSplicing,    // ~@

    // DISPATCHES #
    // Set                         #{}
    Regex,                      // #""
    AnonFn,                     // #()
    Tagged,                     // #symbol
    // ReaderConditionals
    ReaderConditional,          // #?
    ReaderConditionalSplicing,  // #?@

}

public class SyntaxTask implements Runnable, Callable<Integer> {
    final List<Item> items = new ArrayList<>();
    private final Tracer tracer;
    private final Reader in;
    private int numLexerTasks;

    public SyntaxTask(final Tracer tracer, int numLexerTasks, final Reader in) {
        this.tracer = tracer;
        this.in = in;
        this.numLexerTasks = numLexerTasks;
    }

    @Override
    public void run() {
        tracer.info(started(hashCode()).toString());
        LinkedList<Item> brackets = new LinkedList<>();
        for (; ; ) {
            final Object o = in.read();
            if (o instanceof Closer) {
                numLexerTasks--;
                if (numLexerTasks < 1) {
                    break;
                }
                continue;
            }
            final Item item = (Item) o;
            items.add(item);
        }
        tracer.info(finished(hashCode()).toString());
    }

    public int size() {
        return items.size();
    }

    @Override
    public Integer call() throws Exception {
        run();
        return 0;
    }
}

class ParentNode {
    final SyntaxType type;

    public ParentNode(final SyntaxType type) {
        this.type = type;
    }

    public SyntaxType getType() {
        return type;
    }

    public String getValue() {
        return "";
    }
}

class SyntaxNode {
    final SyntaxType type;
    final String value;

    public SyntaxNode(final SyntaxType type, final String value) {
        this.type = type;
        this.value = value;
    }

    public SyntaxType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
