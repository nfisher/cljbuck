package ca.junctionbox.cljbuck.lexer;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.ChannelOutput;

import static ca.junctionbox.cljbuck.lexer.Funcs.lexFile;

public class CharLexer implements CSProcess, Lexable {
    private final String filename;
    private final char[] contents;
    private final ChannelOutput<Object> out;
    private int start;   // start position of this item
    private int pos;     // current position in the contents
    private int line;    // 1+number of newlines seen

    public static final char EOF = 3; // ASCII - ETX/End of Text

    public CharLexer(final String path, final String contents, final ChannelOutput<Object> out) {
        this.filename = path;
        this.contents = contents.toCharArray();
        this.out = out;
    }

    @Override
    public void emit(ItemType t) {
        Item item = new Item(t, start, new String(contents, start, pos-start), line);
        out.write(item);
        start = pos;
    }

    @Override
    public char next() {
        if (pos >= contents.length) {
            return EOF;
        }

        char c = contents[pos];
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
        if (contents[pos] == '\n') {
            line--;
        }
    }

    @Override
    public void ignore() {
        start = pos;
    }

    @Override
    public int getPos() {
        return pos;
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
        out.write(item);
        return null;
    }

    @Override
    public void close() {
        pos = contents.length;
    }

    public void run() {
        leftBrackets.clear();
        StateFunc fn = lexFile;
        for (;;) {
            if (fn == null) break;
            fn = fn.func(this);
        }
    }
}
