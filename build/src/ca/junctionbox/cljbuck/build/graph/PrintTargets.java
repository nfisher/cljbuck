package ca.junctionbox.cljbuck.build.graph;

import ca.junctionbox.cljbuck.build.rules.BuildRule;

import java.io.PrintStream;

public class PrintTargets implements Walken {
    private final PrintStream out;

    public PrintTargets(PrintStream out) {
        this.out = out;
    }

    @Override
    public void step(final BuildRule buildRule, final int depth) {
        System.out.println(buildRule.getName());
    }
}
