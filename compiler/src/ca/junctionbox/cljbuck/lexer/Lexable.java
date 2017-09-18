package ca.junctionbox.cljbuck.lexer;

import java.util.Stack;

public interface Lexable {
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
