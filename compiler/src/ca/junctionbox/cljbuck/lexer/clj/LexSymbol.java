package ca.junctionbox.cljbuck.lexer.clj;

import ca.junctionbox.cljbuck.lexer.ItemType;
import ca.junctionbox.cljbuck.lexer.Lexable;
import ca.junctionbox.cljbuck.lexer.StateFunc;
import ca.junctionbox.cljbuck.lexer.Symbols;

public class LexSymbol implements StateFunc {
    private final StateFunc lexForm;

    public LexSymbol(final StateFunc lexForm) {
        this.lexForm = lexForm;
    }

    public StateFunc func(final Lexable lexable) {
        lexable.acceptRun(Symbols.SYMBOLIC);
        lexable.emit(ItemType.itemSymbol);

        return lexForm;
    }
}
