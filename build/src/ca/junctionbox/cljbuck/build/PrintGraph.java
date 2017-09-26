package ca.junctionbox.cljbuck.build;

import java.io.PrintStream;

public class PrintGraph implements Walken {
    private final PrintStream out;

    PrintGraph(final PrintStream out) {
        this.out = out;
    }

    @Override
    public void step(BuildRule buildRule, int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < depth; i++) {
            sb.append("    |");
        }

        if (0 != depth) {
            sb.append("    +- ");
        }

        sb.append(buildRule.getName());
        sb.append(" - ");
        sb.append(buildRule.getArtefact());

        out.println(sb.toString());
    }
}

