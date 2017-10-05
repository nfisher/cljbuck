package ca.junctionbox.cljbuck.build;

import ca.junctionbox.cljbuck.lexer.Lexeme;
import ca.junctionbox.cljbuck.lexer.StateFunc;

public class BuildFile implements Lexeme {

    @Override
    public StateFunc file() {
        return new LexFile();
    }
}

