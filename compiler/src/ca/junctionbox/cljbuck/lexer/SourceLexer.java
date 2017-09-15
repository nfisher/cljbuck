package ca.junctionbox.cljbuck.lexer;

import java.nio.file.Path;

public interface SourceLexer {
    void lex(Path path, String contents);
}
