package ca.junctionbox.cljbuck.build.graph;

import ca.junctionbox.cljbuck.build.rules.BuildRule;

import java.io.PrintStream;

public class PrintDepsGraph implements Walken {
    private final PrintStream out;

    public PrintDepsGraph(final PrintStream out) {
        this.out = out;
    }

    @Override
    public void step(final BuildRule buildRule, final int depth) {
        final StringBuilder sb = new StringBuilder();
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

