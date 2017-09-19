package ca.junctionbox.cljbuck.lexer;

import org.jcsp.lang.ChannelOutput;

import java.util.Stack;

public interface Lexable {
    char EOF = 3; // ASCII - ETX/End of Text

    static Lexable create(final String path, final String contents, final ChannelOutput<Object> out) {
      return new CharLexer(path, contents, out);
    }

    Stack<Character> leftBrackets = new Stack<>();

    void emit(ItemType t);

    char next();

    char peek();

    void backup();

    void ignore();

    int getPos();

    boolean accept(String valid);

    void acceptRun(String valid);

    StateFunc errorf(String fmt, Object... args);

    void close();

    void run();
}
