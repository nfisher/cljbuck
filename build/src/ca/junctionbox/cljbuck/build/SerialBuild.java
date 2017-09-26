package ca.junctionbox.cljbuck.build;

import java.util.Stack;

public class SerialBuild implements Walken {
    private Stack<BuildRule> buildStack;

    public SerialBuild() {
        this.buildStack = new Stack<>();
    }

    @Override
    public void step(final BuildRule buildRule, final int depth) {
        buildStack.push(buildRule);
    }

    public BuildRule pop() {
        if (buildStack.isEmpty()) {
            return null;
        }
        return buildStack.pop();
    }

    public boolean isEmpty() {
        return buildStack.isEmpty();
    }

    public void build() {
        for (BuildRule n = pop(); n != null; n = pop()) {
            n.build();
        }
    }
}
