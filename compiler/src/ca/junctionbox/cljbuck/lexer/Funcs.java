package ca.junctionbox.cljbuck.lexer;

import com.sun.corba.se.spi.orbutil.fsm.State;

import static ca.junctionbox.cljbuck.lexer.Funcs.*;
import static ca.junctionbox.cljbuck.lexer.StringLexer.EOF;

public class Funcs {
    public static final String UNCLOSED_STRING = "unclosed string";
    public static final String UNCLOSED_KEYWORD = "unclosed keyword";

    static final String OCTAL = "01234567";
    static final String NUMERIC = "012345678";
    static final String HEX = "ABCDEFabcdef" + NUMERIC;
    static final String BASE36 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + NUMERIC;
    static final String WHITESPACE = " \t\r\n";
    static final String BOUNDARY_CHAR = "()[]{}'#\n\r\t ";

    public static final String ALPHA =  "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final String ALPHANUMERIC = ALPHA + NUMERIC;
    public static final String SYMBOLIC = ALPHANUMERIC + "*+!/.-_?:=@><\\%";

    public static final StateFunc lexFile = new LexFile();

    public static StateFunc leftBracket(Lexable l, char c, ItemType t) {
        l.leftBrackets.push(c);
        l.emit(t);
        return lexFile;
    }

    public static StateFunc rightBracket(Lexable l, char cf, ItemType t) {
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
        return lexFile;
    }

    public static final StateFunc lexString = new LexString();

    public static final StateFunc lexKeyword = new LexKeyword();

    public static final StateFunc lexSymbol = new LexSymbol();

    public static final StateFunc lexNumeric = new LexNumeric();

    public static final StateFunc lexComment = new LexComment();


}

