package ca.junctionbox.cljbuck.lexer;

import static ca.junctionbox.cljbuck.lexer.Lexer.EOF;

public class Funcs {
    public static final String UNCLOSED_STRING = "unclosed string";
    public static final String UNCLOSED_KEYWORD = "unclosed keyword";

    static final String OCTAL = "01234567";
    static final String NUMERIC = "012345678";
    static final String HEX = "ABCDEFabcdef" + NUMERIC;
    static final String BASE36 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + NUMERIC;
    static final String WHITESPACE = " \t\r\n";

    public static final String ALPHA =  "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final String ALPHANUMERIC = ALPHA + NUMERIC;
    public static final String SYMBOLIC = ALPHANUMERIC + "*+!/.-_?:";

    public static StateFunc lexText = l -> {
        l.emit(ItemType.itemEOF);
        return null;
    };

    public static StateFunc lexBoolean = l -> {
        l.emit(ItemType.itemBool);
        l.acceptRun("truefals");
        return null;
    };

    public static StateFunc lexString = l -> {
        l.next();
        for (;;) {
            char ch = l.next();
            if ('\"' == ch) break;
            if (EOF == ch) {
                l.errorf(UNCLOSED_STRING);
                return null;
            }
        }
        l.emit(ItemType.itemString);
        return null;
    };

    public static StateFunc lexSymbol = l -> {
        l.acceptRun(SYMBOLIC);
        l.emit(ItemType.itemSymbol);
        return null;
    };

    public static StateFunc lexKeyword = l -> {
        for(;;) {
            char ch = l.next();
            if (' ' == ch) break;
            if (EOF == ch) {
                l.errorf(UNCLOSED_KEYWORD);
                return null;
            }
        }
        l.backup();

        l.emit(ItemType.itemKeyword);
        return null;
    };

    public static StateFunc lexComment = l -> {
        for (;;) {
            char ch = l.next();
            if ('\n' == ch || EOF == ch) break;
        }
        l.emit(ItemType.itemComment);
        return null;
    };

    public static StateFunc lexNumeric = l -> {
        l.accept("-+");
        String digits = NUMERIC;
        boolean isDigital = false;

        if (l.peek() == '.') {
            l.errorf("Invalid Number: %s", " a number should not start with a period");
            return null;
        }

        if (l.accept("0") && l.accept("xX")) {
            digits = HEX;
            isDigital = true;
        } else if (l.accept("3") && l.accept("6") && l.accept("r")) {
            digits = BASE36;
            isDigital = true;
        }
        l.acceptRun(digits);


        if (isDigital && l.accept("./")) {
            l.errorf("Invalid Number: %s", "digital numbers shouldn't have a . or /");
            return null;
        } else if (l.accept("./")) {
            l.acceptRun(digits);
        }

        if (l.accept("eE")) {
            l.accept("+-");
            l.acceptRun(NUMERIC);
        } else {
            l.accept("MN");
        }

        if (l.accept(ALPHANUMERIC)) {
            l.errorf("Invalid Number");
            return null;
        }

        l.emit(ItemType.itemNumber);
        return null;
    };
};
