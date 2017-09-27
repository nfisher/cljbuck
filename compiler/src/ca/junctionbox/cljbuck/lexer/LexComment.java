package ca.junctionbox.cljbuck.lexer;

import static ca.junctionbox.cljbuck.lexer.ItemType.itemComment;
import static ca.junctionbox.cljbuck.lexer.Lexable.EOF;

class LexComment implements StateFunc {
    private final StateFunc parentFn;

    public LexComment(final StateFunc parentFn) {
        this.parentFn = parentFn;
    }

    public StateFunc func(final Lexable l) {
        for (;;) {
            char ch = l.next();
            if ('\n' == ch || '\r' == ch || EOF == ch) break;
        }

        l.emit(itemComment);

        return parentFn;
    }
}
