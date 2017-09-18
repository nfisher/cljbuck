package ca.junctionbox.cljbuck.lexer;

import static ca.junctionbox.cljbuck.lexer.Funcs.lexFile;

public class LexSymbol implements StateFunc {
    public StateFunc func(Lexable lexable) {
        lexable.acceptRun(Funcs.SYMBOLIC);
        lexable.emit(ItemType.itemSymbol);
        return lexFile;
    }
}
