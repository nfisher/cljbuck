package ca.junctionbox.cljbuck.syntax;

import ca.junctionbox.cljbuck.channel.Closer;
import ca.junctionbox.cljbuck.channel.Reader;
import ca.junctionbox.cljbuck.lexer.Item;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class SyntaxTask implements Runnable {
    private final Logger logger;
    private final Reader in;

    final List<Item> items = new ArrayList<>();
    private int numLexerTasks;

    public SyntaxTask(final Logger logger, int numLexerTasks, final Reader in) {
        this.logger = logger;
        this.in = in;
        this.numLexerTasks = numLexerTasks;
    }

    @Override
    public void run() {
        logger.info("started");
        LinkedList<Item> brackets = new LinkedList<>();
        for (;;) {
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
        logger.info("finished");
    }

    public int size() {
        return items.size();
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
