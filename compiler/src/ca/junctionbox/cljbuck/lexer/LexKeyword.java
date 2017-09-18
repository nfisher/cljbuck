package ca.junctionbox.cljbuck.lexer;

import static ca.junctionbox.cljbuck.lexer.Funcs.ALPHANUMERIC;
import static ca.junctionbox.cljbuck.lexer.Funcs.lexFile;

public class LexKeyword implements StateFunc {
    public StateFunc func(Lexable l) {
        l.acceptRun(ALPHANUMERIC + ":./-_?");
        l.emit(ItemType.itemKeyword);
        return lexFile;
    }
}
