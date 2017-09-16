package ca.junctionbox.cljbuck.lexer;

import org.jcsp.lang.CSProcess;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ConsumeTask implements CSProcess {
    final List<Item> items = new ArrayList<>();
    final Lexable l;
    final TreeNode root = new Branch(Syntax.CljFile);

    ConsumeTask(Lexable l) {
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
}

enum Syntax {
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

interface TreeNode {
    boolean hasChildren();
    Syntax getType();
}

class Leaf implements TreeNode {
    private final Syntax type;

    public Leaf(final Syntax type) {
       this.type = type;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public Syntax getType() {
        return type;
    }
}

class Branch implements TreeNode {
    final LinkedList<TreeNode> children = new LinkedList<>();
    final Syntax type;

    Branch(final Syntax type) {
        this.type = type;
    }

    @Override
    public boolean hasChildren() {
        return children.size() > 0;
    }

    @Override
    public Syntax getType() {
        return type;
    }
}
