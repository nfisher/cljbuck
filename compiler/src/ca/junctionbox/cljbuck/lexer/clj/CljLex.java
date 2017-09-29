package ca.junctionbox.cljbuck.lexer.clj;

import ca.junctionbox.cljbuck.lexer.StateFunc;

public class CljLex {
    public LexComment comment(final StateFunc parentFn) {
        return new LexComment(parentFn);
    }

    public LexFile file() {
        return new LexFile(this);
    }

    public LexForm form(final LexFile lexFile) {
        return new LexForm(lexFile, this);
    }

    public LexKeyword keyword(final StateFunc lexForm) {
        return new LexKeyword(lexForm);
    }

    public LexNumeric numeric(final StateFunc lexForm) {
        return new LexNumeric(lexForm);
    }

    public LexString string(final StateFunc lexParent) {
        return new LexString(lexParent);
    }

    public LexSymbol symbol(final StateFunc lexForm) {
        return new LexSymbol(lexForm);
    }
}
