package ca.junctionbox.cljbuck.lexer;

public class Item {
   final ItemType type;
   final int pos;
   final String val;
   final int line;

   public Item(final ItemType type, final int pos, final String val, final int line) {
       this.type = type;
       this.pos = pos;
       this.val = val;
       this.line = line;
   }
}
