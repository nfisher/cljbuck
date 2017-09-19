package ca.junctionbox.cljbuck.lexer;

import java.util.EmptyStackException;

import static ca.junctionbox.cljbuck.lexer.Funcs.*;
import static ca.junctionbox.cljbuck.lexer.ItemType.*;
import static ca.junctionbox.cljbuck.lexer.Lexable.EOF;

public class LexFile implements StateFunc {
   public StateFunc func(final Lexable l) {
       if (l.accept(WHITESPACE)) {
           l.acceptRun(WHITESPACE);
           l.ignore();
       }

       final char ch = l.next();

       try {
           if ('(' == ch) {
               l.push(ch);
               l.emit(itemLeftParen);
               return lexForm;
           } else if (')' == ch) {
               final char last = l.pop();

               if ('(' != last) {
                   l.errorf("want (, got %s", last);
                   return null;
               }

               return lexFile;
           } else if (';' == ch) {
               return lexComment;
           } else if (EOF == ch) {
               l.close();
               l.emit(itemEOF);
               return null;
           }

           l.errorf("unexpected character found %s", ch);
           return null;
       } catch(EmptyStackException esex) {
           l.errorf("unmatched paren found %s", ch);
           return null;
       }
    }
}
