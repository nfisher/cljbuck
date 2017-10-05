package ca.junctionbox.cljbuck.build;

import ca.junctionbox.cljbuck.lexer.ItemType;
import ca.junctionbox.cljbuck.lexer.Lexable;
import ca.junctionbox.cljbuck.lexer.Lexeme;
import ca.junctionbox.cljbuck.lexer.StateFunc;

import static ca.junctionbox.cljbuck.lexer.CharLexer.EOF;
import static ca.junctionbox.cljbuck.lexer.Symbols.ALPHANUMERIC;

public class BuildFile implements Lexeme {

    @Override
    public StateFunc file() {
        return new LexFile();
    }
}

