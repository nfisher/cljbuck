package ca.junctionbox.cljbuck.lexer;

import static ca.junctionbox.cljbuck.lexer.Funcs.*;
import static ca.junctionbox.cljbuck.lexer.ItemType.*;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftBracket;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftParen;

public class LexForm implements StateFunc {
   public StateFunc func(final Lexable l) {
        if (l.accept(WHITESPACE)) {
            l.acceptRun(WHITESPACE);
            l.ignore();
        }

        char ch = l.next();

        if (ch == '(') {
            l.push(ch);
            l.emit(itemLeftParen);
            return lexForm;
        } else if (ch == ')') {
            final char want = '(';
            return matchRight(l, ch, want, itemRightParen);
        } else if (ch == '[') {
            l.push(ch);
            l.emit(itemLeftBracket);
            return lexForm;
        } else if (ch == ']') {
            final char want = '[';
            return matchRight(l, ch, want, itemRightBracket);
        } else if (ch == '{') {
            l.push(ch);
            l.emit(itemLeftBrace);
            return lexForm;
        } else if (ch == '}') {
            final char want = '{';
            return matchRight(l, ch, want, itemRightBrace);
        }

        switch(ch) {
            case ';':
                return lexComment;

            case ':':
                return lexKeyword;

            case '"':
                return lexString;

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return lexNumeric;

            case '&':
                return lexSymbol;

            case '\'':
                return lexSymbol;

            case '\\':
                return lexSymbol;

            case '@':
                l.emit(ItemType.itemDeref);
                return lexSymbol;

            case '^':
                l.emit(ItemType.itemMeta);
                return lexForm;

            case '`':
                l.emit(ItemType.itemBackquote);
                return lexForm;

            case '~':
                l.emit(ItemType.itemUnquote);
                return lexForm;

            case '#':
                l.emit(ItemType.itemDispatch);
                return lexForm;

        }

        if (SYMBOLIC.indexOf(ch) >= 0) {
            return lexSymbol;
        } else {
            l.close();
        }

        l.emit(itemEOF);
        return null;
    }

    private StateFunc matchRight(Lexable l, char ch, char want, ItemType type) {
        final char left = l.pop();
        if (want != left) {
            l.errorf("want %s, got %s", want, ch);
            return null;
        }

        l.emit(type);

        if (l.empty()) {
            return lexFile;
        }
        return lexForm;
    }
}
