package ca.junctionbox.cljbuck.lexer;

import static ca.junctionbox.cljbuck.lexer.Funcs.*;
import static ca.junctionbox.cljbuck.lexer.ItemType.*;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftBracket;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftParen;

public class LexForm implements StateFunc {
   public StateFunc func(Lexable l) {
        if (l.accept(WHITESPACE)) {
            l.acceptRun(WHITESPACE);
            l.ignore();
        }

        char ch = l.next();

        if (ch == '(') return leftBracket(l, '(', itemLeftParen);
        else if (ch == '[') return leftBracket(l, '[', itemLeftBracket);
        else if (ch == '{') return leftBracket(l, '{', itemLeftBrace);
        else if (ch == ')') return rightBracket(l, ')', itemRightParen);
        else if (ch == ']') return rightBracket(l, ']', itemRightBracket);
        else if (ch == '}') return rightBracket(l, '}', itemLeftBrace);

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
}
