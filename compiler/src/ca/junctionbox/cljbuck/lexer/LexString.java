package ca.junctionbox.cljbuck.lexer;

import static ca.junctionbox.cljbuck.lexer.Funcs.UNCLOSED_STRING;
import static ca.junctionbox.cljbuck.lexer.Funcs.lexForm;
import static ca.junctionbox.cljbuck.lexer.Lexable.EOF;

public class LexString implements StateFunc {
    public StateFunc func(Lexable l) {
        l.next();
        for (;;) {
            char ch = l.next();
            if ('\"' == ch) break;
            if (EOF == ch) {
                l.errorf(UNCLOSED_STRING);
                return null;
            }
        }

        l.emit(ItemType.itemString);
        return lexForm;
    }
}