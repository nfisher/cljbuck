package ca.junctionbox.cljbuck.lexer;

import ca.junctionbox.cljbuck.channel.Writer;

public interface Lexable {
    char EOF = 3; // ASCII - ETX/End of Text

    static Lexable create(final String path, final String contents, final Lexeme lexeme, final Writer out) {
        return new CharLexer(path, contents, out, lexeme);
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
