package ca.junctionbox.cljbuck.build;

import ca.junctionbox.cljbuck.lexer.ItemType;
import ca.junctionbox.cljbuck.lexer.Lexable;
import ca.junctionbox.cljbuck.lexer.StateFunc;

import static ca.junctionbox.cljbuck.lexer.CharLexer.EOF;
import static ca.junctionbox.cljbuck.lexer.Symbols.ALPHANUMERIC;

public class LexFile implements StateFunc {

    @Override
    public StateFunc func(final Lexable l) {
        if (l.accept(" \t\n\r,")) {
            l.acceptRun(" \t\n\r,");
            l.ignore();
        }

        final char ch = l.next();

        if ('(' == ch) {
            l.push(ch);
            l.emit(ItemType.itemLeftParen);
            return this;
        } else if (')' == ch) {
            l.pop();
            l.emit(ItemType.itemRightParen);
            return this;
        } else if ('[' == ch) {
            l.push(ch);
            l.emit(ItemType.itemLeftBracket);
            return this;
        } else if (']' == ch) {
            l.pop();
            l.emit(ItemType.itemRightBracket);
            return this;
        } else if ('"' == ch) {
            l.ignore();
            for (;;) {
                final char c = l.next();
                if ('"' == c) {
                    l.backup();
                    l.emit(ItemType.itemString);
                    l.next();
                    l.ignore();
                    break;
                }
            }
            return this;
        } else if (':' == ch) {
            l.acceptRun(ALPHANUMERIC + '-');
            l.emit(ItemType.itemKeyword);
            return this;
        } else if ((ALPHANUMERIC + '-').indexOf(ch) != -1) {
            l.acceptRun(ALPHANUMERIC + '-');
            l.emit(ItemType.itemSymbol);
            return this;
        } else if (';' == ch) {
            for (;;) {
                final char c = l.next();
                if ('\n' == c) {
                    break;
                }
            }
            return this;
        } else if (EOF == ch) {
            l.emit(ItemType.itemEOF);
            return null;
        }

        l.errorf("unexpected character %X '%c'", (int)ch, ch);

        return null;
    }
}
