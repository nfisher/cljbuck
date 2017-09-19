package ca.junctionbox.cljbuck.lexer;

public class LexNumeric implements StateFunc {
    public StateFunc func(Lexable l) {
        ItemType type = ItemType.itemLong;
        String digits = Funcs.NUMERIC;

        l.accept("-+");

        if (l.accept("0")) {
            if (l.accept("xX")) {
                l.acceptRun(Funcs.HEX);
            } else if (l.accept(Funcs.OCTAL)) {
                l.accept(Funcs.OCTAL);
            }
        } else {
            l.acceptRun(Funcs.NUMERIC);
        }

        if (l.accept("/")) {
            l.acceptRun(Funcs.NUMERIC);
            type = ItemType.itemRational;
        } else if (l.accept(".")) {
            type = ItemType.itemDouble;
            l.acceptRun(digits);
        } else if (l.accept("N")) {
            type = ItemType.itemBigInt;
        }

        if (type != ItemType.itemBigInt && type != ItemType.itemRational && l.accept("Ee")) {
            l.accept("-+");
            l.acceptRun(Funcs.NUMERIC);
        } else if (type != ItemType.itemBigInt && type != ItemType.itemRational && l.accept("M")) {
            type = ItemType.itemBigDecimal;
        }

        char ch = l.peek();
        if (Funcs.BOUNDARY_CHAR.indexOf(ch) == -1) {
            l.errorf("NumberFormatException Invalid number");
            return null;
        }

        l.emit(type);
        return Funcs.lexForm;
    }
}
