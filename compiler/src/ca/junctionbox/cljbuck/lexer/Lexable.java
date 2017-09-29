package ca.junctionbox.cljbuck.lexer;

import ca.junctionbox.cljbuck.channel.Writer;
import ca.junctionbox.cljbuck.lexer.clj.CljLex;

public interface Lexable {
    char EOF = 3; // ASCII - ETX/End of Text

    static Lexable create(final String path, final String contents, final CljLex cljLex, final Writer out) {
        return new CharLexer(path, contents, out, cljLex);
    }

    static Lexable create(final String path, final String contents, final Writer out) {
        return new CharLexer(path, contents, out, new CljLex());
    }

    Character pop();

    void push(final Character ch);

    boolean empty();

    void emit(ItemType t);

    char next();

    char peek();

    void backup();

    void ignore();

    int getPos();

    String getFilename();

    boolean accept(String valid);

    void acceptRun(String valid);

    StateFunc errorf(String fmt, Object... args);

    void close();

    void run();
}
