package ca.junctionbox.cljbuck.lexer;

public class Item {
   ItemType type;
   int pos;
   String val;
   int line;

   public Item(ItemType type, int pos, String val, int line) {
       this.type = type;
       this.pos = pos;
       this.val = val;
       this.line = line;
   }
}
