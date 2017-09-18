package ca.junctionbox.cljbuck.lexer;

import static ca.junctionbox.cljbuck.lexer.StringLexer.EOF;

class LexComment implements StateFunc {

    public StateFunc func(Lexable l) {
        for (;;) {
            char ch = l.next();
            if ('\n' == ch || EOF == ch) break;
        }
        l.emit(ItemType.itemComment);
        return Funcs.lexFile;
    }
}
