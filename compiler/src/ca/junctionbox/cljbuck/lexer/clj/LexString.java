package ca.junctionbox.cljbuck.lexer.clj;

import ca.junctionbox.cljbuck.lexer.ItemType;
import ca.junctionbox.cljbuck.lexer.Lexable;
import ca.junctionbox.cljbuck.lexer.StateFunc;

import static ca.junctionbox.cljbuck.lexer.Lexable.EOF;

public class LexString implements StateFunc {
    public static final String UNCLOSED_STRING = "unclosed string";
    private final StateFunc lexParent;

    public LexString(final StateFunc lexParent) {
        this.lexParent = lexParent;
    }

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

        return lexParent;
    }
}
