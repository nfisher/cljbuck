package ca.junctionbox.cljbuck.lexer;

import static ca.junctionbox.cljbuck.lexer.Funcs.lexForm;

public class LexSymbol implements StateFunc {
    public StateFunc func(Lexable lexable) {
        lexable.acceptRun(Funcs.SYMBOLIC);
        lexable.emit(ItemType.itemSymbol);
        return lexForm;
    }
}
