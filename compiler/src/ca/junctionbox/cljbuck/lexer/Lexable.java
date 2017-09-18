package ca.junctionbox.cljbuck.lexer;

import java.util.Stack;

public interface Lexable {
    void emit(ItemType t);

    char next();

    char peek();

    void backup();

    void ignore();

    int getPos();

    boolean accept(String valid);

    void acceptRun(String valid);

    StateFunc errorf(String fmt, Object... args);

    Item nextItem();

    void drain();

    Stack<Character> leftBrackets = new Stack<>();

    void close();
}
