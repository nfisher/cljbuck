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
    public static final String SYMBOLIC = ALPHANUMERIC + "*+!/.-_?:=@><\\%";

    public static StateFunc lexText = l -> {
        if (l.accept(WHITESPACE)) {
            l.acceptRun(WHITESPACE);
            l.ignore();
        }

        if (l.accept(";")) { return Funcs.lexComment;
        } else if(l.accept("[")) {
            return leftBracket(l, '[', ItemType.itemLeftBracket);

        } else if(l.accept("(")) {
            return leftBracket(l, '(', ItemType.itemLeftParen);

        } else if(l.accept("{")) {
            return leftBracket(l, '{', ItemType.itemLeftBrace);

        } else if(l.accept("]")) {
            return rightBracket(l, ']', ItemType.itemRightBracket);

        } else if(l.accept(")")) {
            return rightBracket(l, ')', ItemType.itemRightParen);

        } else if(l.accept("}")) {
            return rightBracket(l, '}', ItemType.itemLeftBrace);

        } else if(l.accept(":")){
            return Funcs.lexKeyword;

        } else if(l.accept("\"")){
            return Funcs.lexString;

        } else if(l.accept(".")) {
            return Funcs.lexSymbol;

        } else if(l.accept("^")) {
            return Funcs.lexSymbol;

        } else if(l.accept("'#&")) {
            l.ignore();
            return Funcs.lexText; // TODO: Need to emit this as distinct symbol

        } else if(l.accept(NUMERIC)){
            return Funcs.lexNumeric;

        } else if(l.accept(SYMBOLIC)) {
            return Funcs.lexSymbol;

        } else {
            char ch = l.peek();
            if (ch != EOF) {
                //System.out.println("\t ch == " + (int) ch);
            }
            l.pos = l.input.length();
        }

        l.emit(ItemType.itemEOF);
        return null;
    };

    public static StateFunc leftBracket(Lexer l, char c, ItemType t) {
        l.leftBrackets.push(c);
        l.emit(t);
        return lexText;
    }

    public static StateFunc rightBracket(Lexer l, char cf, ItemType t) {
        boolean matches = false;
        char c = ' ';

        if (!l.leftBrackets.empty()) {
            c = l.leftBrackets.pop();
        }

        if (c == '[') matches = ']' == cf;
        else if (c == '(') matches = ')' == cf;
        else if (c == '{') matches = '}' == cf;

        if (!matches) {
            l.errorf("want pair for %s, got %s", c, cf);
            return null;
        }
        l.emit(t);
        return lexText;
    }

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
        return lexText;
    };

    public static StateFunc lexSymbol = l -> {
        l.acceptRun(SYMBOLIC);
        l.emit(ItemType.itemSymbol);
        return lexText;
    };

    public static StateFunc lexKeyword = l -> {
        l.acceptRun(ALPHANUMERIC + ":./-_?");
        l.emit(ItemType.itemKeyword);
        return lexText;
    };

    public static StateFunc lexComment = l -> {
        for (;;) {
            char ch = l.next();
            if ('\n' == ch || EOF == ch) break;
        }
        l.emit(ItemType.itemComment);
        return lexText;
    };

    public static StateFunc lexNumeric = l -> {
        l.accept("-+");
        String digits = NUMERIC;
        boolean isDigital = false;

        if (l.accept(".")) {
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
        return lexText;
    };


};
