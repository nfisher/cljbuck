package ca.junctionbox.cljbuck.lexer;

import static ca.junctionbox.cljbuck.lexer.Symbols.ALPHANUMERIC;
import static ca.junctionbox.cljbuck.lexer.Symbols.BOUNDARY_CHAR;

public class LexKeyword implements StateFunc {
    private final StateFunc lexForm;

    public LexKeyword(final StateFunc lexForm) {
        this.lexForm = lexForm;
    }

    public StateFunc func(Lexable l) {
        l.accept(ALPHANUMERIC);
        l.acceptRun(ALPHANUMERIC + ":.-_?");
        if (l.accept("/")) {
            l.acceptRun(ALPHANUMERIC + ":.-_?");
        }

        char c = l.peek();
        if (BOUNDARY_CHAR.indexOf(c) == -1) {
            l.errorf("unexpected character in keyword %s", c);
            return null;
        }

        l.emit(ItemType.itemKeyword);
        return lexForm;
    }
}
