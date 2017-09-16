package ca.junctionbox.cljbuck.lexer;

public interface Lexable {
    void emit(ItemType t);

    char next();

    char peek();

    void backup();

    void ignore();

    boolean accept(String valid);

    void acceptRun(String valid);

    StateFunc errorf(String fmt, Object... args);

    Item nextItem();

    void drain();
}
