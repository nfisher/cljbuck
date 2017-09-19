package ca.junctionbox.cljbuck.lexer;

import java.util.EmptyStackException;

public class Funcs {
    public static final String UNCLOSED_STRING = "unclosed string";
    public static final String UNCLOSED_KEYWORD = "unclosed keyword";

    static final String OCTAL = "01234567";
    static final String NUMERIC = "012345678";
    static final String HEX = NUMERIC + "ABCDEFabcdef";
    static final String BASE36 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + NUMERIC;
    static final String WHITESPACE = " \t\r\n";
    static final String BOUNDARY_CHAR = "()[]{}'#~@`^;\\\n\r\t ";

    public static final String ALPHA =  "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    // optimise for common case of lower-case letters then numbers, and finally upper-case letters.
    public static final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyz" + NUMERIC + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String SYMBOLIC = "abcdefghijklmnopqrstuvwxyz-." + NUMERIC + "ABCDEFGHIJKLMNOPQRSTUVWXYZ*+!/_?:=@><\\%";

    public static final StateFunc lexForm = new LexForm();

    public static StateFunc leftBracket(final Lexable l, final char c, final ItemType t) {
        l.push(c);
        l.emit(t);
        return lexForm;
    }

    public static StateFunc rightBracket(final Lexable l, final char cf, final ItemType t) {
        try {
            final char c = l.pop();
            boolean matches = false;

            if (c == '[') {
                matches = (']' == cf);
            } else if (c == '(') {
                matches = (')' == cf);
            } else if (c == '{') {
                matches = ('}' == cf);
            }

            if (!matches) {
                l.errorf("want pair for %s, got %s", c, cf);
                return null;
            }
            l.emit(t);

            if (l.empty()) { // no nesting, use file lexer
                return lexFile;
            }
            return lexForm;
        } catch(final EmptyStackException ese) {
            l.errorf("Mismatched bracket");
            return null;
        }
    }

    public static final StateFunc lexString = new LexString();

    public static final StateFunc lexKeyword = new LexKeyword();

    public static final StateFunc lexSymbol = new LexSymbol();

    public static final StateFunc lexNumeric = new LexNumeric();

    public static final StateFunc lexComment = new LexComment();

    public static final StateFunc lexFile = new LexFile();
}

