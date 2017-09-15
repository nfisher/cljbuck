package ca.junctionbox.cljbuck.lexer;

import org.jcsp.lang.CSProcess;

import java.util.ArrayList;
import java.util.List;

public class ConsumeTask implements CSProcess {
    final List<Item> items = new ArrayList<>();
    final Lexer l;

    ConsumeTask(Lexer l) {
        this.l = l;
    }

    @Override
    public void run() {
        for (;;) {
            final Item item = l.nextItem();
            if (item == null) {
                break;
            }
            items.add(item);
        }
    }
}
