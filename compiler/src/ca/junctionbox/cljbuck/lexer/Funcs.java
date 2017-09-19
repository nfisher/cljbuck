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

    public static final StateFunc lexString = new LexString();

    public static final StateFunc lexKeyword = new LexKeyword();

    public static final StateFunc lexSymbol = new LexSymbol();

    public static final StateFunc lexNumeric = new LexNumeric();

    public static final StateFunc lexComment = new LexComment();

    public static final StateFunc lexFile = new LexFile();
}

