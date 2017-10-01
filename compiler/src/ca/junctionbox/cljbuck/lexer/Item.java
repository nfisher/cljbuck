package ca.junctionbox.cljbuck.lexer;

public class Item {
    final ItemType type;
    final int pos;
    final int line;
    final String val;
    final String filename;

    public Item(final ItemType type, final int pos, final String val, final int line, final String filename) {
        this.type = type;
        this.pos = pos;
        this.val = val;
        this.line = line;
        this.filename = filename;
    }
}
