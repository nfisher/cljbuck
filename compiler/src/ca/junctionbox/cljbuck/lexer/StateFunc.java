package ca.junctionbox.cljbuck.lexer;

public interface StateFunc {
    StateFunc func(final Lexable lexable);
}
