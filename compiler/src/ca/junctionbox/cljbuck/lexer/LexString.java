package ca.junctionbox.cljbuck.lexer;

import static ca.junctionbox.cljbuck.lexer.Funcs.UNCLOSED_STRING;
import static ca.junctionbox.cljbuck.lexer.Funcs.lexFile;
import static ca.junctionbox.cljbuck.lexer.Funcs.lexForm;
import static ca.junctionbox.cljbuck.lexer.Lexable.EOF;

public class LexString implements StateFunc {
    public StateFunc func(final Lexable l) {
        l.next();
        for (;;) {
            char ch = l.next();
            if ('\\' == ch && l.accept("\"")) {
               continue;
            } else if ('\\' == ch && l.accept("\\")) {
               continue;
            }

            if ('\"' == ch) break;
            if (EOF == ch) {
                l.errorf(UNCLOSED_STRING);
                return null;
            }
        }

        l.emit(ItemType.itemString);
        if (l.empty()) {
            return lexFile;
        }
        return lexForm;
    }
}
