package ca.junctionbox.cljbuck.lexer;

public class Symbols {
    public static final String ALPHA =  "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final String OCTAL = "01234567";
    static final String NUMERIC = "012345678";
    public static final String SYMBOLIC = "abcdefghijklmnopqrstuvwxyz-." + NUMERIC + "ABCDEFGHIJKLMNOPQRSTUVWXYZ*+!/_?:=@><\\%&";
    // optimise for common case of lower-case letters then numbers, and finally upper-case letters.
    public static final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyz" + NUMERIC + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final String BASE36 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + NUMERIC;
    static final String HEX = NUMERIC + "ABCDEFabcdef";
    static final String WHITESPACE = " \t\r\n";
    static final String BOUNDARY_CHAR = "()[]{}'#~@`^;\\\n\r\t ";
}
