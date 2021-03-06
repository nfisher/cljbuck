package ca.junctionbox.cljbuck.lexer;

import ca.junctionbox.cljbuck.channel.Writer;

import java.util.Stack;

public class CharLexer implements Lexable {
    public static final char EOF = 3; // ASCII - ETX/End of Text
    private final String filename;
    private final char[] contents;
    private final Writer out;
    private final Stack<Character> brackets;
    private final Lexeme lexeme;
    private int start;   // start position of this item
    private int pos;     // current position in the contents
    private int line;    // 1+number of newlines seen

    public CharLexer(final String path, final String contents, final Writer out, final Lexeme lexeme) {
        this.filename = path;
        this.contents = contents.toCharArray();
        this.out = out;
        this.lexeme = lexeme;
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
        final Item item = new Item(t, start, new String(contents, start, pos - start), line, filename);
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
            if (i > 1 && i % 1_000 == 0) {
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
        StateFunc fn = lexeme.file();
        for (int i = 0; ; i++) {
            if (fn == null) {
                break;
            }
            fn = fn.func(this);
        }
    }
}
