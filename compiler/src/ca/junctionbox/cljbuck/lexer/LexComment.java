package ca.junctionbox.cljbuck.lexer;

import static ca.junctionbox.cljbuck.lexer.Funcs.lexFile;
import static ca.junctionbox.cljbuck.lexer.Funcs.lexForm;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemComment;
import static ca.junctionbox.cljbuck.lexer.Lexable.EOF;

class LexComment implements StateFunc {

    public StateFunc func(Lexable l) {
        for (;;) {
            char ch = l.next();
            if ('\n' == ch || '\r' == ch || EOF == ch) break;
        }

        l.emit(itemComment);

        if (l.empty()) {
            return lexFile;
        }
        return lexForm;
    }
}
