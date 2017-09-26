package ca.junctionbox.cljbuck.build;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Stack;

public class PrintGraph implements Walken {
    private final PrintStream out;

    PrintGraph(final PrintStream out) {
        this.out = out;
    }

    @Override
    public void step(Node node, int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < depth; i++) {
            sb.append("    |");
        }

        if (0 != depth) {
            sb.append("    +- ");
        }

        sb.append(node.getName());
        sb.append(" - ");
        sb.append(node.getArtefact());

        out.println(sb.toString());
    }
}

