package ca.junctionbox.cljbuck.lexer;

import static ca.junctionbox.cljbuck.lexer.Funcs.ALPHANUMERIC;
import static ca.junctionbox.cljbuck.lexer.Funcs.BOUNDARY_CHAR;
import static ca.junctionbox.cljbuck.lexer.Funcs.lexForm;

public class LexKeyword implements StateFunc {
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
