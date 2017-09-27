package ca.junctionbox.cljbuck.lexer;

import org.jcsp.lang.CSProcess;

import java.util.Stack;

public class CharLexer implements CSProcess, Lexable {
    private final String filename;
    private final char[] contents;
    private final Writer out;
    private final Stack<Character> brackets;
    private final CljLex cljLex;
    private int start;   // start position of this item
    private int pos;     // current position in the contents
    private int line;    // 1+number of newlines seen

    public static final char EOF = 3; // ASCII - ETX/End of Text

    public CharLexer(final String path, final String contents, final Writer out, final CljLex cljLex) {
        this.filename = path;
        this.contents = contents.toCharArray();
        this.out = out;
        this.cljLex = cljLex;
        this.brackets = new Stack<>();
    }

    public void push(final Character c) {
        brackets.push(c);
    }

    public Character pop() {
        return brackets.pop();
    }

    public boolean empty() {
        return brackets.empty();
    }

    @Override
    public void emit(ItemType t) {
        Item item = new Item(t, start, new String(contents, start, pos - start), line, filename);
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
    public String getFilename() {
        return filename;
    }

    @Override
    public boolean accept(final String valid) {
        char ch = next();
        if (EOF == ch) {
            return false;
        }
        if (valid.indexOf(ch) != -1) {
            return true;
        }

        backup();
        return false;
    }

    @Override
    public void acceptRun(final String valid) {
        for (int i = 0; ; i++) {
            char ch = next();
            if (i > 0 && i % 10_00_000 == 0) {
                System.out.println(getFilename() + " is taking a long time with " + ch);
            }
            if (EOF == ch) {
                return;
            }
            if (valid.indexOf(ch) == -1) {
                break;
            }
        }
        backup();
    }

    @Override
    public StateFunc errorf(final String fmt, Object... args) {
        Item item = new Item(ItemType.itemError, start, String.format(fmt, args), line, filename);
        out.write(item);
        return null;
    }

    @Override
    public void close() {
        pos = contents.length;
    }

    public void run() {
        StateFunc fn = cljLex.file();
        for (int i = 0; ; i++) {
            if (i > 1 && i % 10_000_000 == 0)
                System.out.println("::run() " + getFilename() + " is taking a long time " + fn + " pos: " + getPos());
            if (fn == null) break;
            fn = fn.func(this);
        }
    }
}
