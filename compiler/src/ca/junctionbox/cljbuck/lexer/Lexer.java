package ca.junctionbox.cljbuck.lexer;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Channel;
import org.jcsp.lang.One2OneChannel;
import org.jcsp.util.Buffer;

import java.nio.file.Path;

import static ca.junctionbox.cljbuck.channel.Closer.close;
import static ca.junctionbox.cljbuck.lexer.Funcs.lexText;

public class Lexer implements CSProcess {
    private final One2OneChannel<Object> items;
    private String name;
    private String input;
    int start;
    int pos;
    int lastPos;
    int line;

    public static final char EOF = 3; // ASCII - ETX/End of Text

    public Lexer(String path, String contents) {
        this.items = Channel.one2one();
        this.name = path;
        this.input = contents;
    }

    public void emit(ItemType t) {
        Item item = new Item(t, start, input.substring(start, pos), line);
        items.out().write(item);
        start = pos;
    }

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

    public char peek() {
        char c = next();
        backup();
        return c;
    }

    public void backup() {
        pos -= 1;
        if (input.charAt(pos) == '\n') {
            line--;
        }
    }

    public void ignore() {
        start = pos;
    }

    public boolean accept(final String valid) {
        if (valid.indexOf(next()) >= 0) {
            return true;
        }

        backup();
        return false;
    }

    public void acceptRun(final String valid) {
        for(;;) {
            if (valid.indexOf(next()) == -1) break;
        }
        backup();
    }

    public StateFunc errorf(final String fmt, Object... args) {
        Item item = new Item(ItemType.itemError, start, String.format(fmt, args), line);
        items.out().write(item);
        return null;
    }

    public Item nextItem() {
        Item item = (Item) items.in().read();
        lastPos = item.pos;
        return item;
    }

    public void drain() {
        for(;nextItem() != null;) { }
    }

    public void run() {
        for (StateFunc fn = lexText; fn != null;) {
            fn = fn.func(this);
        }
        close(items.out());
    }
}
