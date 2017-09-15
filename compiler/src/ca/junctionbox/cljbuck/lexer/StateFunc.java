package ca.junctionbox.cljbuck.lexer;

public interface StateFunc {
    StateFunc func(Lexer lexer);
}
