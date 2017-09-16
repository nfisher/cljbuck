package ca.junctionbox.cljbuck.lexer;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Channel;
import org.jcsp.lang.One2OneChannel;
import org.jcsp.util.Buffer;

import java.util.Stack;

import static ca.junctionbox.cljbuck.channel.Closer.close;
import static ca.junctionbox.cljbuck.lexer.Funcs.lexFile;

public class Lexer implements CSProcess, Lexable {
    private final One2OneChannel<Object> items;
    String name;
    String input;
    Stack<Character> leftBrackets;
    int start;
    int pos;
    int lastPos;
    int line;

    public static final char EOF = 3; // ASCII - ETX/End of Text

    public Lexer(String path, String contents) {
        this.items = Channel.one2one(new Buffer(8192));
        this.name = path;
        this.input = contents;
        this.leftBrackets = new Stack();
    }

    @Override
    public void emit(ItemType t) {
        Item item = new Item(t, start, input.substring(start, pos), line);
        items.out().write(item);
        start = pos;
    }

    @Override
    public char next() {
        if (pos >= input.length()) {
            return EOF;
        }

        char c = input.charAt(pos);
        pos += 1;
        if ('\n' == c) {
           line++;
        }
        return c;
    }

    @Override
    public char peek() {
        char c = next();
        backup();
        return c;
    }

    @Override
    public void backup() {
        pos -= 1;
        if (input.charAt(pos) == '\n') {
            line--;
        }
    }

    @Override
    public void ignore() {
        start = pos;
    }

    @Override
    public boolean accept(final String valid) {
        char ch = next();
        if (EOF == ch) return false;
        if (valid.indexOf(ch) != -1) {
            return true;
        }

        backup();
        return false;
    }

    @Override
    public void acceptRun(final String valid) {
        for(;;) {
            char ch = next();
            if (EOF == ch) break;
            if (valid.indexOf(ch) == -1) break;
        }
        backup();
    }

    @Override
    public StateFunc errorf(final String fmt, Object... args) {
        Item item = new Item(ItemType.itemError, start, String.format(fmt, args), line);
        items.out().write(item);
        return null;
    }

    @Override
    public Item nextItem() {
        Item item = (Item) items.in().read();
        if (item == null) return null;

        lastPos = item.pos;
        return item;
    }

    @Override
    public void drain() {
        for(;nextItem() != null;) { }
    }

    public void run() {
        try {
            StateFunc fn = lexFile;
            for (;;) {
                if (fn == null) break;
                fn = fn.func(this);
            }
        } finally {
            close(items.out());
        }
    }
}
