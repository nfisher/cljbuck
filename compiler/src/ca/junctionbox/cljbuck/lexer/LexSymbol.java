package ca.junctionbox.cljbuck.lexer;

public class LexSymbol implements StateFunc {
    private final StateFunc lexForm;

    public LexSymbol(final StateFunc lexForm) {
        this.lexForm = lexForm;
    }

    public StateFunc func(final Lexable lexable) {
        lexable.acceptRun(Symbols.SYMBOLIC);
        lexable.emit(ItemType.itemSymbol);

        return lexForm;
    }
}
