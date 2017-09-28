package ca.junctionbox.cljbuck.lexer.clj;

import ca.junctionbox.cljbuck.lexer.ItemType;
import ca.junctionbox.cljbuck.lexer.Lexable;
import ca.junctionbox.cljbuck.lexer.StateFunc;
import ca.junctionbox.cljbuck.lexer.Symbols;

import static ca.junctionbox.cljbuck.lexer.Symbols.BOUNDARY_CHAR;

public class LexNumeric implements StateFunc {
    private final StateFunc lexForm;

    public LexNumeric(final StateFunc lexForm) {
        this.lexForm = lexForm;
    }

    public StateFunc func(final Lexable l) {
        ItemType type = ItemType.itemLong;
        String digits = Symbols.NUMERIC;

        l.accept("-+");

        if (l.accept("0")) {
            if (l.accept("xX")) {
                l.acceptRun(Symbols.HEX);
                char ch = l.peek();
                if (BOUNDARY_CHAR.indexOf(ch) == -1) {
                    l.errorf("NumberFormatException Invalid number");
                    return null;
                }
            } else if (l.accept(Symbols.OCTAL)) {
                l.accept(Symbols.OCTAL);
                char ch = l.peek();
                if (BOUNDARY_CHAR.indexOf(ch) == -1) {
                    l.errorf("NumberFormatException Invalid number");
                    return null;
                }
            }
        } else {
            l.acceptRun(Symbols.NUMERIC);
        }

        if (l.accept("/")) {
            l.acceptRun(Symbols.NUMERIC);
            type = ItemType.itemRational;
        } else if (l.accept(".")) {
            type = ItemType.itemDouble;
            l.acceptRun(digits);
        } else if (l.accept("N")) {
            type = ItemType.itemBigInt;
        }

        if (type != ItemType.itemBigInt && type != ItemType.itemRational && l.accept("Ee")) {
            l.accept("-+");
            l.acceptRun(Symbols.NUMERIC);
        } else if (type != ItemType.itemBigInt && type != ItemType.itemRational && l.accept("M")) {
            type = ItemType.itemBigDecimal;
        }

        char ch = l.peek();
        if (BOUNDARY_CHAR.indexOf(ch) == -1) {
            l.errorf("NumberFormatException Invalid number");
            return null;
        }

        l.emit(type);

        return lexForm;
    }
}
