package ca.junctionbox.cljbuck.lexer;

import org.jcsp.lang.ChannelOutput;

public interface Lexable {
    char EOF = 3; // ASCII - ETX/End of Text

    static Lexable create(final String path, final String contents, final ChannelOutput<Object> out) {
        return new CharLexer(path, contents, new WriterChannel(out));
    }

    static Lexable create(final String path, final String contents, final WriterQueue out) {
        return new CharLexer(path, contents, out);
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
