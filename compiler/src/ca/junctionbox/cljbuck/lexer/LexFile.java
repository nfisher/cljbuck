package ca.junctionbox.cljbuck.lexer;

import static ca.junctionbox.cljbuck.lexer.Funcs.*;
import static ca.junctionbox.cljbuck.lexer.StringLexer.EOF;

public class LexFile implements StateFunc {
   public StateFunc func(Lexable l) {
        if (l.accept(WHITESPACE)) {
            l.acceptRun(WHITESPACE);
            l.ignore();
        }

        if (l.accept(";")) {
            return Funcs.lexComment;
        } else if (l.accept("[")) {
            return leftBracket(l, '[', ItemType.itemLeftBracket);

        } else if (l.accept("(")) {
            return leftBracket(l, '(', ItemType.itemLeftParen);

        } else if (l.accept("{")) {
            return leftBracket(l, '{', ItemType.itemLeftBrace);

        } else if (l.accept("]")) {
            return rightBracket(l, ']', ItemType.itemRightBracket);

        } else if (l.accept(")")) {
            return rightBracket(l, ')', ItemType.itemRightParen);

        } else if (l.accept("}")) {
            return rightBracket(l, '}', ItemType.itemLeftBrace);

        } else if (l.accept(":")) {
            return Funcs.lexKeyword;

        } else if (l.accept("\"")) {
            return Funcs.lexString;

        } else if (l.accept(".")) {
            return Funcs.lexSymbol;

        } else if (l.accept("^")) {
            return Funcs.lexSymbol;

        } else if (l.accept("'#&")) {
            l.ignore();
            return lexFile; // TODO: Need to emit this as distinct symbol

        } else if (l.accept(NUMERIC)) {
            return Funcs.lexNumeric;  // TODO: Need to handle +/-

        } else if (l.accept(SYMBOLIC)) {
            return Funcs.lexSymbol;

        } else {
            char ch = l.peek();
            if (ch != EOF) {
                //System.out.println("\t ch == " + (int) ch);
            }
            l.close();
        }

        l.emit(ItemType.itemEOF);
        return null;
    }
}
