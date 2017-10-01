package ca.junctionbox.cljbuck.lexer;

public class Symbols {
    public static final String ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String OCTAL = "01234567";
    public static final String NUMERIC = "012345678";
    public static final String SYMBOLIC = "abcdefghijklmnopqrstuvwxyz-." + NUMERIC + "ABCDEFGHIJKLMNOPQRSTUVWXYZ*+!/_?:=@><\\%&";
    // optimise for common case of lower-case letters then numbers, and finally upper-case letters.
    public static final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyz" + NUMERIC + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String BASE36 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + NUMERIC;
    public static final String HEX = NUMERIC + "ABCDEFabcdef";
    public static final String WHITESPACE = " \t\r\n";
    public static final String BOUNDARY_CHAR = "()[]{}'#~@`^;\\\n\r\t ";
}
