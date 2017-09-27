package ca.junctionbox.cljbuck.lexer;

import java.util.EmptyStackException;

import static ca.junctionbox.cljbuck.lexer.ItemType.*;
import static ca.junctionbox.cljbuck.lexer.Lexable.EOF;

public class LexFile implements StateFunc {
    private final StateFunc lexComment;
    private final StateFunc lexForm;

    public LexFile(final CljLex cljLex) {
        this.lexComment = cljLex.comment(this);
        this.lexForm = cljLex.form(this);
    }

    public StateFunc func(final Lexable l) {
        if (l.accept(Symbols.WHITESPACE)) {
            l.acceptRun(Symbols.WHITESPACE);
            l.ignore();
        }

        final char ch = l.next();

        try {
            if ('(' == ch) {
                l.push(ch);
                l.emit(itemLeftParen);
                return lexForm;
            } else if (')' == ch) {
                final char last = l.pop();

                if ('(' != last) {
                    l.errorf("want (, got %s", last);
                    return null;
                }

                return this;
            } else if (';' == ch) {
                return lexComment;
            } else if (EOF == ch) {
                l.close();
                l.emit(itemEOF);
                return null;
            }

            l.errorf("unexpected character found %s", ch);
            return null;
        } catch (EmptyStackException esex) {
            l.errorf("unmatched paren found %s", ch);
            return null;
        }
    }
}
