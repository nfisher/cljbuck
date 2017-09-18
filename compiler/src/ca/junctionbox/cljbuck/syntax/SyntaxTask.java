package ca.junctionbox.cljbuck.syntax;

import ca.junctionbox.cljbuck.lexer.Item;
import ca.junctionbox.cljbuck.lexer.Lexable;
import org.jcsp.lang.CSProcess;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.google.common.graph.GraphBuilder;

public class SyntaxTask implements CSProcess {
    final List<Item> items = new ArrayList<>();
    final Lexable l;

    public SyntaxTask(final Lexable l) {
        this.l = l;
    }

    @Override
    public void run() {
        LinkedList<Item> brackets = new LinkedList<>();
        for (;;) {
            final Item item = l.nextItem();
            if (item == null) {
                break;
            }
            items.add(item);
        }
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

    Quote,              // '
    Backslash,          // \
    Comment,            // ;
    Deref,              // @
    Metadata,           // ^
    Backquote,          // `
    Unquote,            // ~
    UnquoteSplicing,    // ~@

    // DISPATCHES #
    Regex,                      // #""
    AnonFn,                     // #()
    Tagged,                     // #symbol
    // ReaderConditionals
    ReaderConditional,          // #?
    ReaderConditionalSplicing,  // #?@

}
