package ca.junctionbox.cljbuck.lexer.clj;

import ca.junctionbox.cljbuck.lexer.ItemType;
import ca.junctionbox.cljbuck.lexer.Lexable;
import ca.junctionbox.cljbuck.lexer.StateFunc;
import ca.junctionbox.cljbuck.lexer.Symbols;

import java.util.EmptyStackException;

import static ca.junctionbox.cljbuck.lexer.ItemType.*;

public class LexForm implements StateFunc {
    private final LexComment lexComment;
    private final StateFunc lexFile;
    private final LexForm lexForm;
    private final LexKeyword lexKeyword;
    private final LexSymbol lexSymbol;
    private final LexNumeric lexNumeric;
    private final LexString lexString;

    public LexForm(final StateFunc lexFile, final CljLex cljLex) {
        this.lexComment = cljLex.comment(this);
        this.lexFile = lexFile;
        this.lexForm = this;
        this.lexKeyword = cljLex.keyword(this);
        this.lexString = cljLex.string(this);
        this.lexSymbol = cljLex.symbol(this);
        this.lexNumeric = cljLex.numeric(this);
    }

    public StateFunc func(final Lexable l) {
        if (l.accept(Symbols.WHITESPACE)) {
            l.acceptRun(Symbols.WHITESPACE);
            l.ignore();
        }

        char ch = l.next();

        try {
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
        } catch (EmptyStackException esex) {
            l.errorf("unmatched bracket found %s", ch);
            return null;
        }

        switch (ch) {
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

            case '\'':
                l.emit(ItemType.itemQuote);
                return lexForm;

            case '\\':
                l.emit(ItemType.itemLiteral);
                return lexSymbol;

            case '@':
                l.emit(ItemType.itemDeref);
                return lexSymbol;

            case '^':
                l.emit(ItemType.itemMeta);
                return lexForm;

            case '`':
                l.emit(ItemType.itemBacktick);
                return lexForm;

            case '~':
                l.emit(ItemType.itemUnquote);
                return lexForm;

            case '#':
                l.emit(ItemType.itemDispatch);
                return lexForm;

        }

        if (Symbols.SYMBOLIC.indexOf(ch) >= 0) {
            return lexSymbol;
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
